package com.ecommerce.payentservice.mapper;

import com.ecommerce.payentservice.dto.PaymentDTO;
import com.ecommerce.payentservice.entity.BillingEntity;
import org.springframework.stereotype.Component;

@Component
public class PaymentMapper {
    public PaymentDTO toPaymentDTO(BillingEntity billing) {
        return PaymentDTO.builder()
                .email(billing.getEmail())
                .firstName(billing.getFirstName())
                .lastName(billing.getLastName())
                .address(billing.getAddress())
                .country(billing.getCountry())
                .states(billing.getStates())
                .zipCode(billing.getZipCode())
                .companyName(billing.getCompany())
                .phoneNumber(billing.getPhoneNumber())
                .build();
    }
}
