package com.parking.identity_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAccessTokenRequest {
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String code;
    String code_verifier;
    String client_id;
    String client_secret;
    String redirect_uri;
    final String grant_type = "authorization_code";
}
