package com.parking.ticket_service.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TicketResponse {
    int turnTotal;

    String contentPlate;

    String buyTime;

    String expireTime;

    String ticketId;

    String name;

    String vehicle;

    String status;

    int price;

    String usedTime;

    long buyAt;

    long startAt;

    String startTime;

    long expireAt;

    long usedAt;

    String plate;

    String email;

    String unit;
}
