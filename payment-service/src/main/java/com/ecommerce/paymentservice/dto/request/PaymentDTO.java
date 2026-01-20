package com.ecommerce.paymentservice.dto.request;

import lombok.Data;

@Data
public class PaymentDTO {
    private long amount;
    private String bankCode;
    private String language;
}
