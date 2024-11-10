package com.parking.profile_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerProfileResponse {

    String uid;

    String name;

    String phone;

    String email;

    int isPhoneActive;

    String avatar;
}
