package com.ecommerce.inventoryservice.dto.request;

import com.ecommerce.inventoryservice.entity.InventoryType;
import lombok.Data;

@Data
public class InventoryAdjustRequest {

    private String skuCode;

    private InventoryType type; // IMPORT | EXPORT | ADJUST

    private Integer quantity;

    private String note; // ghi chú tuỳ chọn
}
