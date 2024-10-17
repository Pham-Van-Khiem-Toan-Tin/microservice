package com.ecommerce.payentservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/payment")
public class PaymentController {
    @GetMapping("/profile")
    public ResponseEntity<String> myPayment() {
        return ResponseEntity.ok("test");
    }
}
