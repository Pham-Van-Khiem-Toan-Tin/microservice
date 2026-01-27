package com.ecommerce.orderservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class OrderItemCheckForm {
    private String orderId;
    private String skuCode;
    private Integer quantity;
}
