package com.parking.vault_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Document(collection = "fluctuation")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Fluctuation {

    @Id
    String id;

    String depositId;

    String ownerId;

    String reason;

    String description;

    int amount;

    String transaction;

    long createAt;
}
