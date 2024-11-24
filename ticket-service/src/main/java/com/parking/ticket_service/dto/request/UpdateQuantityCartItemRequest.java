package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateQuantityCartItemRequest {
    @NotNull(message = "FIELD_INFORMATION_MISSING")
    String ticketId;

    @NotNull(message = "FIELD_INFORMATION_MISSING")
    @Min(value = 1, message = "QUANTITY_TOO_SMALL")
    @Max(value = 20, message = "QUANTITY_TOO_LAG")
    Integer quantity;
}
