package com.ecommerce.payentservice.controller;

import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.form.UpdateBillingForm;
import com.ecommerce.payentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "/payment")
public class PaymentController {
    @Autowired
    PaymentService paymentService;

    @GetMapping("/profile")
    public ResponseEntity<PaymentDTO> viewPayment() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(paymentService.getProfile(userId));
    }
    @PutMapping("/profile")
    public ResponseEntity<PaymentDTO> editPayment(@RequestBody UpdateBillingForm form) {
        return ResponseEntity.ok(paymentService.updateProfile(form));
    }
}
