package com.parking.profile_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerProfileUpdateRequest {
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String name;

    String phone;
}
