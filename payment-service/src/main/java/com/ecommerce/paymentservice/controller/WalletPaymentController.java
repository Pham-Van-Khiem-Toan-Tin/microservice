package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.constants.Constants;
import com.ecommerce.paymentservice.dto.request.InternalPaymentForm;
import com.ecommerce.paymentservice.dto.response.ApiResponse;
import com.ecommerce.paymentservice.service.WalletService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/wallet/pay")
public class WalletPaymentController {
    @Autowired
    private WalletService walletService;
    @PostMapping
    public ApiResponse<Void> orderPayment(@RequestBody InternalPaymentForm internalPaymentForm) throws JsonProcessingException {
        walletService.processWalletPayment(internalPaymentForm);
        return ApiResponse.ok(Constants.WALLET_PAY_SUCCESS);
    }
}
