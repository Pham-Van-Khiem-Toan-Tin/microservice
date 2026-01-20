package com.ecommerce.identityservice.integration;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;

@FeignClient(name = "payment-service", url = "${application.config.payment-url}")
public interface PaymentFeignClient {

    @GetMapping("/wallets/balance")
    BigDecimal getWalletBalance();
}
