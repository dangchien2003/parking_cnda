package com.parking.ticket_service.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.hibernate.validator.constraints.Length;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class TicketUpdatePlateRequest {
    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    @Length(max = 10, message = "Biển số có độ dài tối đã 10 ký tự")
    String plate;

    @NotBlank(message = "FIELD_INFORMATION_MISSING")
    String ticketId;
}
