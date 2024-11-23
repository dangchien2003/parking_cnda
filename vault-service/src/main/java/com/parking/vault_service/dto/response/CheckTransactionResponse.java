package com.parking.vault_service.dto.response;

import lombok.*;
import lombok.experimental.FieldDefaults;

@FieldDefaults(level = AccessLevel.PRIVATE)
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class CheckTransactionResponse {
    String orderId;
}
