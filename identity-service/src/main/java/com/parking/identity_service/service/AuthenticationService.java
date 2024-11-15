package com.parking.identity_service.service;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.parking.identity_service.dto.request.*;
import com.parking.identity_service.dto.response.*;
import com.parking.identity_service.entity.InvalidatedToken;
import com.parking.identity_service.entity.User;
import com.parking.identity_service.enums.EBlock;
import com.parking.identity_service.exception.AppException;
import com.parking.identity_service.exception.ErrorCode;
import com.parking.identity_service.repository.InvalidatedRepository;
import com.parking.identity_service.repository.UserRepository;
import com.parking.identity_service.repository.httpclient.GoogleProfileClient;
import com.parking.identity_service.repository.httpclient.GoogleTokenClient;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Slf4j
public class AuthenticationService {
    PasswordEncoder passwordEncoder;
    UserRepository userRepository;
    GoogleTokenClient googleTokenClient;
    GoogleProfileClient googleProfileClient;
    InvalidatedRepository invalidatedRepository;

    @NonFinal
    @Value("${jwt.signer-key}")
    String signerKey;

    @NonFinal
    @Value("${jwt.valid-duration}")
    long validDuration;

    @NonFinal
    @Value("${jwt.refreshable-duration}")
    long refreshabledDuration;

    @NonFinal
    @Value("${jwt.issuer}")
    String issuer;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    String clientId;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    String clientSecret;

    @NonFinal
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    String redirectUri;

    public RefreshTokenResponse refreshToken(RefreshTokenRequest request)
            throws JOSEException {

        String uid;
        String jti;
        Date expiryTime;
        try {
            SignedJWT signedJWT = verifyToken(request.getToken(), true);

            uid = signedJWT.getJWTClaimsSet().getSubject();
            jti = signedJWT.getJWTClaimsSet().getJWTID();
            expiryTime = signedJWT.getJWTClaimsSet().getExpirationTime();
        } catch (ParseException | AppException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        User user = userRepository.findById(uid)
                .orElseThrow(() ->
                        new AppException(ErrorCode.USER_NOT_EXIST));

        invalidatedRepository.save(new InvalidatedToken(jti, expiryTime));

        return RefreshTokenResponse.builder()
                .token(genToken(user))
                .build();
    }

    public void logout(LogoutRequest request) {
        String token = request.getToken();

        try {
            SignedJWT signedJWT = verifyToken(token, false);

            String jti = signedJWT.getJWTClaimsSet().getJWTID();

            Date date = signedJWT.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = InvalidatedToken.builder()
                    .idToken(jti)
                    .expiryTime(date)
                    .build();

            invalidatedRepository.save(invalidatedToken);
        } catch (ParseException | AppException e) {
            log.warn("token already expired");
        } catch (JOSEException e) {
            log.error(e.getMessage());
        }
    }

    public IntrospectResponse introspect(IntrospectRequest request)
            throws JOSEException {
        boolean isValid = true;

        try {
            verifyToken(request.getToken(), false);
        } catch (AppException | ParseException e) {
            isValid = false;
        }

        return IntrospectResponse.builder()
                .valid(isValid)
                .build();
    }

    public AuthenticationResponse authentication(AuthenticationRequest request) {

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword()))
            throw new AppException(ErrorCode.USER_NOT_EXIST);

        if (user.getIsBlocked() == EBlock.BLOCKED.getValue())
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);

        String token = genToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    public GoogleUserProfileResponse getInfoGoogleAccount(String authorizationCode, String codeVerifier) {
        GoogleAccessTokenRequest googleAccessTokenRequest =
                createBodyGoogleApiGetAccessToken(authorizationCode, codeVerifier);

        GoogleAccessTokenResponse response;
        try {
            response = googleTokenClient.getAccessToken(googleAccessTokenRequest);
        } catch (Exception e) {
            log.error("error: ", e);
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }
        return googleProfileClient.getProfile("Bearer " + response.getAccess_token());
    }

    public AuthenticationResponse googleAuthentication(GoogleAuthenticationRequest request) {
        GoogleUserProfileResponse googleUserProfileResponse =
                getInfoGoogleAccount(request.getAuthorizationCode(), request.getCodeVerifier());

        User user = userRepository.findByEmail(googleUserProfileResponse.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_EXIST));

        if (user.getIsBlocked() == EBlock.BLOCKED.getValue())
            throw new AppException(ErrorCode.ACCOUNT_BLOCKED);

        String token = genToken(user);
        return AuthenticationResponse.builder()
                .token(token)
                .build();
    }

    GoogleAccessTokenRequest createBodyGoogleApiGetAccessToken(String authorizationCode, String codeVerifier) {
        return GoogleAccessTokenRequest.builder()
                .code(authorizationCode)
                .code_verifier(codeVerifier)
                .client_id(clientId)
                .client_secret(clientSecret)
                .redirect_uri(redirectUri)
                .build();
    }

    SignedJWT verifyToken(String token, boolean isRefresh)
            throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(signerKey.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        Date expiryTime = !isRefresh
                ? signedJWT.getJWTClaimsSet().getExpirationTime()
                : new Date(signedJWT.getJWTClaimsSet().getIssueTime()
                .toInstant().plus(refreshabledDuration, ChronoUnit.SECONDS)
                .toEpochMilli());

        boolean verified = signedJWT.verify(verifier);

        if (!verified
                || !expiryTime.after(new Date())
                || invalidatedRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())
        ) {
            throw new AppException(ErrorCode.UNAUTHENTICATED);
        }

        return signedJWT;
    }

    String genToken(User user) {

        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);

        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(user.getUid())
                .issuer(issuer)
                .issueTime(new Date())
                .expirationTime(new Date(
                        Instant.now().plus(validDuration, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(user))
                .build();

        Payload payload = new Payload(jwtClaimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(header, payload);

        try {
            jwsObject.sign(new MACSigner(signerKey.getBytes()));
            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
        }
    }

    String buildScope(User user) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if (!CollectionUtils.isEmpty(user.getRoles())) {
            user.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                role.getPermissions().forEach(permission ->
                        stringJoiner.add(permission.getName())
                );
            });
        }
        return stringJoiner.toString();
    }

}
