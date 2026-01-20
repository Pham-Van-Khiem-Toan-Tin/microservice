package com.ecommerce.paymentservice.controller;

import com.ecommerce.paymentservice.constants.Constants;
import com.ecommerce.paymentservice.dto.response.ApiResponse;
import com.ecommerce.paymentservice.dto.response.SePayWebhookDto;
import com.ecommerce.paymentservice.entity.PaymentTransactionEntity;
import com.ecommerce.paymentservice.enums.PaymentType;
import com.ecommerce.paymentservice.repository.PaymentTransactionRepository;
import com.ecommerce.paymentservice.service.SeaPayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/sepay")
public class SePayController {

    @Value("${application.sepay.api-key}")
    private String mySepayApiKey;
    @Autowired
    private SeaPayService seaPayService;


    @PostMapping("/webhook")
    public ResponseEntity<Map<String, Object>> handleSePayWebhook(
            @RequestBody SePayWebhookDto data,
            @RequestHeader(value = "Authorization", required = false) String authorization
    ) {
        String expectedAuth = "Apikey " + mySepayApiKey;
        if (authorization == null || !authorization.equals(expectedAuth)) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "Unauthorized"));
        }

        try {
            String resultMessage = seaPayService.processWebhook(data);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", resultMessage
            ));

        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "Error: " + e.getMessage()
            ));
        }
    }
    @GetMapping("/deposit-qr")
    public ApiResponse<String> getDepositQr(
            @RequestParam long amount,
            @RequestParam(required = false) String referenceId,
            @RequestParam PaymentType type
    ) {

        // Gọi service lấy link ảnh
        String qrUrl = seaPayService.generatePaymentQr(amount, referenceId, type);

        return ApiResponse.ok(Constants.QR_CREATE_SUCCESS, qrUrl);
    }

}
