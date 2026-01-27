package com.ecommerce.inventoryservice.dto.request.order;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemCheckForm {
    private String orderId;
    private String skuCode;
    private Integer quantity;
}
