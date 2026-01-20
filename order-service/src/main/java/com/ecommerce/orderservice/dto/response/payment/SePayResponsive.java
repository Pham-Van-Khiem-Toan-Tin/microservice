package com.ecommerce.orderservice.dto.response.payment;

import lombok.Data;

@Data
public class SePayResponsive {
    private String message;
    private int code;
    private String data;
}
