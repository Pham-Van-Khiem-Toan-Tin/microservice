package com.ecommerce.inventoryservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryAdjustResponse {
    private String skuCode;
    private int totalStock;
    private int reservedStock;
    private int availableStock;
}
