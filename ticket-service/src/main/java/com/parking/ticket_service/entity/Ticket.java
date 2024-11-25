package com.parking.ticket_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "ticket")
public class Ticket {

    @Id
    String id;

    String uid;

    int price;

    String category;

    int turnTotal;

    String contentPlate;

    long buyAt;

    long start;

    long expireAt;

    long cancleAt;

    long usedAt;
}
