package com.ecommerce.orderservice.dto.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InternalPaymentForm {
    private long amount;
    private String orderNumber;
}
