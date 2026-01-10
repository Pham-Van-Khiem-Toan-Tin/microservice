package com.ecommerce.orderservice.dto.request;

import com.ecommerce.orderservice.entity.OrderStatus;
import lombok.Data;

@Data
public class UpdateOrderRequest {
    private OrderStatus status;
    private String reason;
}
