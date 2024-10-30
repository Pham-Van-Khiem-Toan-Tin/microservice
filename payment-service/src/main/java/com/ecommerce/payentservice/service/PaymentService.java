package com.ecommerce.payentservice.service;

import com.ecommerce.payentservice.dto.BillingDTO;

public interface PaymentService {
    BillingDTO getProfile(String customerId);
}
