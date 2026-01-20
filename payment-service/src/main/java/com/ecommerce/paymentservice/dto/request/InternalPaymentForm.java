package com.ecommerce.paymentservice.dto.request;

import lombok.Data;

@Data
public class InternalPaymentForm {
    private long amount;
    private String orderNumber;
}
