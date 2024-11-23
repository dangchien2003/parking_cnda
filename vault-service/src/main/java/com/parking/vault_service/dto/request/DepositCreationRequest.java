package com.parking.vault_service.dto.request;

import jakarta.validation.constraints.Min;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class DepositCreationRequest {

    @Min(value = 10_000, message = "TOO_SMALL_AMOUNT")
    int amount;
}
