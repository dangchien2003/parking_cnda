package com.parking.vault_service.service;

import com.parking.vault_service.configuration.VNPAYConfig;
import com.parking.vault_service.dto.response.CheckTransactionResponse;
import com.parking.vault_service.utils.PaymentUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.*;

@Service
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class VnPayService {


    @NonFinal
    String secretKey = VNPAYConfig.secretKey;

    public String generateUrl(String orderId, int amount, String ipAddress) throws UnsupportedEncodingException {
        String vnpVersion = "2.1.0";
        String vnpCommand = "pay";
        String orderType = "other";

        String vnpTmnCode = VNPAYConfig.vnpTmnCode;

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", vnpVersion);
        vnpParams.put("vnp_Command", vnpCommand);
        vnpParams.put("vnp_TmnCode", vnpTmnCode);
        vnpParams.put("vnp_Amount", String.valueOf(amount * 100));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", orderId);
        vnpParams.put("vnp_OrderInfo", "Thanh toan don hang:" + orderId);
        vnpParams.put("vnp_OrderType", orderType);
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", VNPAYConfig.vnpReturnUrl);
        vnpParams.put("vnp_IpAddr", ipAddress);

        Calendar cld = Calendar.getInstance(TimeZone.getTimeZone("Etc/GMT+7"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        String vnpCreateDate = formatter.format(cld.getTime());
        vnpParams.put("vnp_CreateDate", vnpCreateDate);

        String queryUrl = VNPAYConfig.getQueryUrl(vnpParams);
        return VNPAYConfig.vnpPayUrl + "?" + queryUrl;
    }


    public CheckTransactionResponse queryTransaction(HttpServletRequest request) {
        Map<String, String[]> params = request.getParameterMap();
//        if (!isValidVnPayCallback(params))
//            throw new AppException(ErrorCode.VALIDATE_INFO_PAYMENT_ERROR);

        String vnpRequestId = UUID.randomUUID().toString();
        String vnpVersion = "2.1.0";
        String vnpCommand = "querydr";
        String vnpTmnCode = VNPAYConfig.vnpTmnCode;
        String vnpTxnRef = params.get("vnp_TxnRef")[0];
        String paymentDate = params.get("vnp_PayDate")[0];
        String ipAddress = PaymentUtils.getClientIP(request);
        String vnpOrderInfo = "Kiem tra ket qua GD OrderId:" + vnpTxnRef;

//        Transaction transaction = getTransaction(vnpTxnRef);
//        if (transaction.getStatus().equals(TransactionStatus.SUCCESS.name()))
//            throw new AppException(ErrorCode.TRANSACTION_VERIFIED);
//
//        String vnpCreateDate = TimeUtils.convertTimestampToString(transaction.getCreatedAt(), "yyyyMMddHHmmss");
//
//        String hashData = String.join("|", vnpRequestId, vnpVersion, vnpCommand, vnpTmnCode, vnpTxnRef, paymentDate, vnpCreateDate, ipAddress, vnpOrderInfo);
//        String vnpSecureHash = VNPAYConfig.hmacSHA512(VNPAYConfig.secretKey, hashData);
//
//        VnPayCheckTransactionRequest checkRequest = VnPayCheckTransactionRequest.builder()
//                .vnpRequestId(vnpRequestId)
//                .vnpVersion(vnpVersion)
//                .vnpCommand(vnpCommand)
//                .vnpTmnCode(vnpTmnCode)
//                .vnpTxnRef(vnpTxnRef)
//                .vnpOrderInfo(vnpOrderInfo)
//                .vnpTransactionDate(paymentDate)
//                .vnpCreateDate(vnpCreateDate)
//                .vnpIpAddr(ipAddress)
//                .vnpSecureHash(vnpSecureHash)
//                .build();
//
//        VnPayCheckTransactionResponse response = vnPayClient.checkTransaction(checkRequest);
//
//        if (response.getVnpTransactionStatus() == null ||
//                !response.getVnpTransactionStatus().equals("00") ||
//                !response.getVnpResponseCode().equals("00")) {
//            if (transactionRepository.updateStatus(vnpTxnRef, TransactionStatus.ERROR, Instant.now().toEpochMilli()) != 1) {
//                log.error("update status error fail for transaction: " + vnpTxnRef);
//                throw new AppException(ErrorCode.UPDATE_STATUS_FAIL);
//            }
//            throw new AppException(ErrorCode.TRANSACTION_ERROR);
//        }
//
//        if (transactionRepository.updateStatus(vnpTxnRef, TransactionStatus.SUCCESS, Instant.now().toEpochMilli()) != 1) {
//            log.error("update status success error for transaction: " + vnpTxnRef);
//            throw new AppException(ErrorCode.PAYMENT_SUCCESS_BUT_UPDATE_STATUS_FAIL);
//        }
//
//        try {
//            orderClient.callPaymentSuccess(vnpTxnRef);
//        } catch (Exception e) {
//            log.error("orderClient.callPaymentSuccess error: ", e);
//            throw new AppException(ErrorCode.ERROR_CALL_TO_ORDER_CLIENT);
//        }

        return CheckTransactionResponse.builder()
                .orderId(vnpTxnRef)
                .build();
    }

//    Transaction getTransaction(String orderId) {
//        try {
//            return transactionRepository.getTransaction(orderId);
//        } catch (EmptyResultDataAccessException e) {
//            throw new AppException(ErrorCode.NOTFOUND_DATA);
//        } catch (Exception e) {
//            log.error("transactionRepository.getTransaction error: ", e);
//            throw new AppException(ErrorCode.UNCATEGORIZED_EXCEPTION);
//        }
//    }

    boolean isValidVnPayCallback(Map<String, String[]> queryParams) {
        Map<String, String> flatParams = new LinkedHashMap<>();
        for (Map.Entry<String, String[]> entry : queryParams.entrySet()) {
            flatParams.put(entry.getKey(), entry.getValue()[0]);
        }

        String receivedHash = flatParams.get("vnp_SecureHash");
        if (receivedHash == null) {
            return false;
        }
        flatParams.remove("vnp_SecureHash");

        List<String> keys = new ArrayList<>(flatParams.keySet());

        StringBuilder data = new StringBuilder();
        for (String key : keys) {
//            if (key.equals("vnp_TransactionStatus"))
//                continue;

            if (data.length() > 0) {
                data.append("&");
            }
            data.append(key).append("=").append(flatParams.get(key));
        }

        String calculatedHash = VNPAYConfig.hmacSHA512(secretKey, data.toString());

        return receivedHash.equalsIgnoreCase(calculatedHash);
    }
}
