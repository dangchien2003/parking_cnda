package com.parking.vault_service.service;

import com.parking.vault_service.configuration.VNPAYConfig;
import com.parking.vault_service.dto.request.VnPayCheckTransactionRequest;
import com.parking.vault_service.dto.response.VnPayCheckTransactionResponse;
import com.parking.vault_service.entity.Deposit;
import com.parking.vault_service.repository.httpclient.VnPayClient;
import com.parking.vault_service.utils.PaymentUtils;
import com.parking.vault_service.utils.TimeUtils;
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
    VnPayClient vnPayClient;

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


    public int checkPaymentSuccess(HttpServletRequest request, Deposit deposit) {
        Map<String, String[]> params = request.getParameterMap();

        String vnpRequestId = UUID.randomUUID().toString();
        String vnpVersion = "2.1.0";
        String vnpCommand = "querydr";
        String vnpTmnCode = VNPAYConfig.vnpTmnCode;
        String vnpTxnRef = params.get("vnp_TxnRef")[0];
        String paymentDate = params.get("vnp_PayDate")[0];
        String ipAddress = PaymentUtils.getClientIP(request);
        String vnpOrderInfo = "Kiem tra ket qua GD OrderId:" + vnpTxnRef;

        String vnpCreateDate = TimeUtils.convertTimestampToString(deposit.getCreateAt(), "yyyyMMddHHmmss");

        String hashData = String.join("|", vnpRequestId, vnpVersion, vnpCommand, vnpTmnCode, vnpTxnRef, paymentDate, vnpCreateDate, ipAddress, vnpOrderInfo);
        String vnpSecureHash = VNPAYConfig.hmacSHA512(VNPAYConfig.secretKey, hashData);

        VnPayCheckTransactionRequest checkRequest = VnPayCheckTransactionRequest.builder()
                .vnpRequestId(vnpRequestId)
                .vnpVersion(vnpVersion)
                .vnpCommand(vnpCommand)
                .vnpTmnCode(vnpTmnCode)
                .vnpTxnRef(vnpTxnRef)
                .vnpOrderInfo(vnpOrderInfo)
                .vnpTransactionDate(paymentDate)
                .vnpCreateDate(vnpCreateDate)
                .vnpIpAddr(ipAddress)
                .vnpSecureHash(vnpSecureHash)
                .build();

        VnPayCheckTransactionResponse response = vnPayClient.checkTransaction(checkRequest);

        if (response.getVnpAmount() / 100 == deposit.getAmount()) {
            if (response.getVnpTransactionStatus().equals("00") &&
                    response.getVnpResponseCode().equals("00")) {
                return 1;
            } else if (response.getVnpTransactionStatus().equals("11")) {
                return 2;
            }
        }

        return 0;
    }

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
