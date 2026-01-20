package com.ecommerce.orderservice.dto.response.payment;

import lombok.Data;

@Data
public class PaymentResponse {
    private String status;
    private String message;
    private String url;
}
