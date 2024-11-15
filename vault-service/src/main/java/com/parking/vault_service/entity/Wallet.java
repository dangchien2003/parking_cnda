package com.parking.vault_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Document(collection = "wallet")
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Wallet {
    @Id
            @GeneratedValue(strategy = GenerationType.UUID)
    String id;

    String ownerId;

    String status;

    Long modifiedAt;

    String modifiedBy;

    String description;
}
