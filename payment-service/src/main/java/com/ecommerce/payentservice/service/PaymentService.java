package com.ecommerce.payentservice.service;


import com.ecommerce.payentservice.dto.PaymentDTO;

public interface PaymentService {
    PaymentDTO getProfile(String userId);
}
