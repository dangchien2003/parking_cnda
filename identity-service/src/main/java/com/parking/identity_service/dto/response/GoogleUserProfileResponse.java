package com.parking.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserProfileResponse {
    String sub;
    String name;
    String given_name;
    String family_name;
    String picture;
    String email;
    boolean email_verified;
}
