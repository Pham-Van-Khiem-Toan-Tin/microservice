package com.ecommerce.payentservice.service;


import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.form.UpdateBillingForm;

public interface PaymentService {
    PaymentDTO getProfile(String userId);
    PaymentDTO updateProfile(UpdateBillingForm form);
}
