package com.ecommerce.payentservice.controller;

import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @GetMapping("/profile")
    public ResponseEntity<PaymentDTO> myPayment(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaim("sub");
        return ResponseEntity.ok(paymentService.getProfile(userId));
    }
}
