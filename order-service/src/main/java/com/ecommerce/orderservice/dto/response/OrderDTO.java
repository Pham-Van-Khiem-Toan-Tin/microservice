package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderDTO {
    private UUID id;
    private String orderNumber;
    private BigDecimal finalAmount;
    private OrderStatus orderStatus;
    private PaymentStatus paymentStatus;
}
