package com.ecommerce.inventoryservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class OrderItemCheckForm {
    private String skuCode;
    private Integer quantity;
}
