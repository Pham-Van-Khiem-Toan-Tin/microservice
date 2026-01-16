package com.ecommerce.inventoryservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class InventoryDTO {
    private String id;
    private String skuCode;

    private int totalStock;
    private int reservedStock;
    private int availableStock;

    private Integer minStockLevel;

    private boolean lowStock;
    private InventoryStatus status;

}
