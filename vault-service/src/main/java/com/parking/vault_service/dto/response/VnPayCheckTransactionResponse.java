package com.parking.vault_service.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
@JsonIgnoreProperties(ignoreUnknown = true)
public class VnPayCheckTransactionResponse {
    @JsonProperty("vnp_ResponseId")
    String vnpResponseId;

    @JsonProperty("vnp_Command")
    String vnpCommand;

    @JsonProperty("vnp_TmnCode")
    String vnpTmnCode;

    @JsonProperty("vnp_TxnRef")
    String vnpTxnRef;

    @JsonProperty("vnp_Amount")
    int vnpAmount;

    @JsonProperty("vnp_OrderInfo")
    String vnpOrderInfo;

    @JsonProperty("vnp_ResponseCode")
    String vnpResponseCode;

    @JsonProperty("vnp_Message")
    String vnpMessage;

    @JsonProperty("vnp_BankCode")
    String vnpBankCode;

    @JsonProperty("vnp_PayDate")
    String vnpPayDate;

    @JsonProperty("vnp_TransactionNo")
    String vnpTransactionNo;

    @JsonProperty("vnp_TransactionType")
    String vnpTransactionType;

    @JsonProperty("vnp_TransactionStatus")
    String vnpTransactionStatus;

    @JsonProperty("vnp_PromotionCode")
    String vnpPromotionCode;

    @JsonProperty("vnp_PromotionAmount")
    String vnpPromotionAmount;

    @JsonProperty("vnp_SecureHash")
    String vnpSecureHash;
}

