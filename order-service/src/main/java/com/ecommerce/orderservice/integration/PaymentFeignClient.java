package com.ecommerce.orderservice.integration;

import com.ecommerce.orderservice.dto.request.InternalPaymentForm;
import com.ecommerce.orderservice.dto.response.ApiResponse;
import com.ecommerce.orderservice.dto.response.order.WalletResponse;
import com.ecommerce.orderservice.dto.response.payment.PaymentResponse;
import com.ecommerce.orderservice.dto.response.payment.SePayResponsive;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(
        name = "payment-service",
        url = "${application.config.payment-url}"
)
public interface PaymentFeignClient {
    @GetMapping("/vnpay/create-payment")
    PaymentResponse getVnpayUrl(
            @RequestParam("amount") long amount,
            @RequestParam("bankCode") String bankCode,
            @RequestParam("type") String type,
            @RequestParam("referenceId") String referenceId,
            @RequestParam("ipAddress") String ipAddress
    );
    @GetMapping("/api/sepay/deposit-qr")
    SePayResponsive getSepayUrl(
            @RequestParam("amount") long amount,
            @RequestParam("type") String type,
            @RequestParam("referenceId") String referenceId
    );
    @PostMapping("/wallet/pay")
    WalletResponse payInternalOrder(InternalPaymentForm internalPaymentForm) throws JsonProcessingException;

}
