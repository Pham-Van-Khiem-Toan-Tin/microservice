package com.ecommerce.inventoryservice.dto.response;

import com.ecommerce.inventoryservice.entity.InventoryType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class InventoryHistoryDTO {
    private String id;
    private String skuCode;
    private Integer quantityChange;
    private Integer stockAfter;
    private InventoryType type;
    private String referenceId;
    private Instant createdAt;
}
