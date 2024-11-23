package com.parking.ticket_service.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity(name = "cart")
public class Cart {
    @EmbeddedId
    CartId id;

    @NotNull
    int quantity;

    @NotNull
    Long createdAt;
    Long modifiedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = Instant.now().toEpochMilli();
    }

    @PreUpdate
    protected void onUpdate() {
        modifiedAt = Instant.now().toEpochMilli();
    }
}
