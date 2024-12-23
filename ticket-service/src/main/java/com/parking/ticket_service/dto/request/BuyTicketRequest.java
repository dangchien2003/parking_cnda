package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class BuyTicketRequest {
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String category;

    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String startDate;

    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String endDate;
}
