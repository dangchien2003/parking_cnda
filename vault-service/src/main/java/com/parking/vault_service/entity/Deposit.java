package com.parking.vault_service.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import lombok.*;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Indexed;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
//@Document(collection = "deposit")
@Entity
public class Deposit {

    @Id
    String id;

    String ownerId;

    int amount;

    long createAt;

    String actionBy;

    Long actionAt;

    Long cancelAt;
}
