package com.parking.ticket_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
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

    @ManyToOne
    @NotNull
    Category category;

    int turnTotal;

    String contentPlate;

    long buyAt;

    long startAt;

    long expireAt;

    long usedAt;
}
