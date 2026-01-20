package com.ecommerce.orderservice.dto.response.inventory;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class InventoryDto {
    private String skuCode;
    private Integer quantity;
}
