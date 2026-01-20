package com.ecommerce.inventoryservice.dto.response.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InventoryCartDto {
    private String skuCode;
    private Integer quantity;
}
