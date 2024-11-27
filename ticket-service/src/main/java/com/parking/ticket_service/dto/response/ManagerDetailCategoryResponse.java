package com.parking.ticket_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ManagerDetailCategoryResponse {
    String id;
    String name;
    String vehicle;
    String type;
    String timeEnd;
    int price;
}
