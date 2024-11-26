package com.parking.ticket_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlateResponse {
    int turn;

    String contentPlate;

    String urlPrefixCode;

    String imageIn;

    long checkinAt;

    String imageOut;

    long checkoutAt;

    long usedAt;

    String checkinTime;
    String checkoutTime;

}
