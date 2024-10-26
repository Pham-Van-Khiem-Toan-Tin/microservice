package com.ecommerce.payentservice.service.impl;

import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.entity.BillingEntity;
import com.ecommerce.payentservice.repository.PaymentRepository;
import com.ecommerce.payentservice.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;
    @Override
    public PaymentDTO getProfile(String userId) {
        PaymentDTO paymentDTO = new PaymentDTO();
        BillingEntity payment = paymentRepository.findByCustomerId(userId);
        if (payment != null) {

        }
        return paymentDTO;
    }
}
