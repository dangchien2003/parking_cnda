package com.parking.vault_service.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class VnPayCheckTransactionRequest {
    @JsonProperty("vnp_RequestId")
    String vnpRequestId;

    @JsonProperty("vnp_Version")
    String vnpVersion;

    @JsonProperty("vnp_Command")
    String vnpCommand;

    @JsonProperty("vnp_TmnCode")
    String vnpTmnCode;

    @JsonProperty("vnp_TxnRef")
    String vnpTxnRef;

    @JsonProperty("vnp_OrderInfo")
    String vnpOrderInfo;

    @JsonProperty("vnp_TransactionDate")
    String vnpTransactionDate;

    @JsonProperty("vnp_CreateDate")
    String vnpCreateDate;

    @JsonProperty("vnp_IpAddr")
    String vnpIpAddr;

    @JsonProperty("vnp_SecureHash")
    String vnpSecureHash;
}
