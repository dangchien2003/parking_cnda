package com.parking.identity_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ProfileCustomer {
    String uid;

    String name;

    String phone;

    int isPhoneActive;
}
