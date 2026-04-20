package com.planbookai.backend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class VNPayConfig {

    @Value("${vnpay.tmn-code}")
    private String tmnCode;

    @Value("${vnpay.hash-secret}")
    private String hashSecret;

    @Value("${vnpay.payment-url:https://sandbox.vnpayment.vn/paymentv2/vpcpay.html}")
    private String paymentUrl;

    @Value("${vnpay.return-url}")
    private String returnUrl;

    @Value("${vnpay.ipn-url:}")
    private String ipnUrl;

    @Value("${vnpay.version:2.1.0}")
    private String version;

    public String getTmnCode()    { return tmnCode; }
    public String getHashSecret() { return hashSecret; }
    public String getPaymentUrl() { return paymentUrl; }
    public String getReturnUrl()  { return returnUrl; }
    public String getIpnUrl()     { return ipnUrl; }
    public String getVersion()    { return version; }
}
