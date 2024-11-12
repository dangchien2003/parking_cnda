package com.parking.vault_service.entity;

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
//@Document(collection = "owner")
@Entity
public class Owner {
    @Id
    String id;

    int balance;

    Long createAt;
}
