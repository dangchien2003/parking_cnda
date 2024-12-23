package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CategoryUpdateRequest {

    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String id;

    @Min(value = 0, message = "INVALID_DATA")
    int price;
}
