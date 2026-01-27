package com.ecommerce.paymentservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InternalPaymentForm {
    private long amount;
    private String orderNumber;
}
