package com.parking.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class GoogleAccessTokenResponse {
    String access_token;
    int expires_in;
    String scope;
    String token_type;
    String id_token;
}
