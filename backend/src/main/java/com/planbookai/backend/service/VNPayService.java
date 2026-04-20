package com.planbookai.backend.service;

import com.planbookai.backend.config.VNPayConfig;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * VNPayService – sinh payment URL và xác thực chữ ký callback từ VNPay.
 *
 * SPEC quan trọng (từ VNPay official Java sample):
 *  - params được sort theo key alphabetically (TreeMap)
 *  - hashData = key=URLEncode(value)&key=URLEncode(value)  (UTF-8, space→+)
 *  - queryString = key=URLEncode(value)&key=URLEncode(value) (cùng encoding)
 *  - HMAC-SHA512(hashSecret, hashData) → vnp_SecureHash
 */
@Service
public class VNPayService {

    private final VNPayConfig config;

    public VNPayService(VNPayConfig config) {
        this.config = config;
    }

    public String createPaymentUrl(String txnRef, long amount, String orderInfo, String clientIp) {
        TimeZone tz = TimeZone.getTimeZone("Asia/Ho_Chi_Minh");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
        sdf.setTimeZone(tz);

        Date now = new Date();
        String vnpCreateDate = sdf.format(now);
        String vnpExpireDate = expireDate(sdf, now, 15);

        // orderInfo: chỉ dùng ký tự ASCII để tránh encoding mismatch
        String safeOrderInfo = toAscii(orderInfo);

        // TreeMap → tự sort alphabetically theo key
        Map<String, String> params = new TreeMap<>();
        params.put("vnp_Amount",     String.valueOf(amount * 100));
        params.put("vnp_Command",    "pay");
        params.put("vnp_CreateDate", vnpCreateDate);
        params.put("vnp_CurrCode",   "VND");
        params.put("vnp_ExpireDate", vnpExpireDate);
        params.put("vnp_IpAddr",     clientIp);
        params.put("vnp_Locale",     "vn");
        params.put("vnp_OrderInfo",  safeOrderInfo);
        params.put("vnp_OrderType",  "other");
        params.put("vnp_ReturnUrl",  config.getReturnUrl());
        params.put("vnp_TmnCode",    config.getTmnCode());
        params.put("vnp_TxnRef",     txnRef);
        params.put("vnp_Version",    config.getVersion());

        // hashData và queryString dùng CÙNG encoding (URLEncoder UTF-8)
        StringBuilder hashData     = new StringBuilder();
        StringBuilder queryBuilder = new StringBuilder();

        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key   = entry.getKey();
            String value = entry.getValue();
            if (value == null || value.isEmpty()) continue;

            // URLEncoder.encode(value, UTF-8): space → +, ':' → %3A, '/' → %2F
            String encoded = URLEncoder.encode(value, StandardCharsets.UTF_8);

            if (!hashData.isEmpty()) {
                hashData.append('&');
                queryBuilder.append('&');
            }
            hashData.append(key).append('=').append(encoded);
            queryBuilder.append(key).append('=').append(encoded);
        }

        String secureHash = hmacSHA512(config.getHashSecret(), hashData.toString());
        queryBuilder.append("&vnp_SecureHash=").append(secureHash);

        return config.getPaymentUrl() + "?" + queryBuilder;
    }

    /**
     * Xác thực callback từ VNPay.
     * Spring đã decode query params → ta nhận raw value.
     * Phải encode lại trước khi tính hash (giống lúc tạo URL).
     */
    public boolean validateSignature(Map<String, String> params) {
        String receivedHash = params.get("vnp_SecureHash");
        if (receivedHash == null) return false;

        Map<String, String> sortedParams = new TreeMap<>(params);
        sortedParams.remove("vnp_SecureHash");
        sortedParams.remove("vnp_SecureHashType");

        StringBuilder hashData = new StringBuilder();
        for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
            String value = entry.getValue();
            if (value == null || value.isEmpty()) continue;
            if (!hashData.isEmpty()) hashData.append('&');
            // Encode lại để khớp với lúc tạo URL
            hashData.append(entry.getKey()).append('=')
                    .append(URLEncoder.encode(value, StandardCharsets.UTF_8));
        }

        String computedHash = hmacSHA512(config.getHashSecret(), hashData.toString());
        return computedHash.equalsIgnoreCase(receivedHash);
    }

    // ─────────────────────────────────────────────────────────────────────────

    private String hmacSHA512(String key, String data) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] bytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("HMAC-SHA512 error", e);
        }
    }

    private String expireDate(SimpleDateFormat sdf, Date base, int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(base);
        cal.add(Calendar.MINUTE, minutes);
        return sdf.format(cal.getTime());
    }

    /** Strip Vietnamese diacritics → ASCII safe string cho orderInfo */
    private String toAscii(String input) {
        if (input == null) return "";
        return input
            .replaceAll("[àáâãäăắặấầẩẫậ]", "a").replaceAll("[ÀÁÂÃÄĂẮẶẤẦẨẪẬ]", "A")
            .replaceAll("[èéêëếềệểễ]", "e").replaceAll("[ÈÉÊËẾỀỆỂỄ]", "E")
            .replaceAll("[ìíîïị]", "i").replaceAll("[ÌÍÎÏỊ]", "I")
            .replaceAll("[òóôõöơộốồổỗờớợởỡ]", "o").replaceAll("[ÒÓÔÕÖƠỘỐỒỔỖỜỚỢỞỠ]", "O")
            .replaceAll("[ùúûüưữựứừử]", "u").replaceAll("[ÙÚÛÜƯỮỰỨỪỬ]", "U")
            .replaceAll("[ýỳỷỹỵ]", "y").replaceAll("[ÝỲỶỸỴ]", "Y")
            .replaceAll("[đ]", "d").replaceAll("[Đ]", "D")
            .replaceAll("[^\\x20-\\x7E]", "")
            .trim();
    }
}
