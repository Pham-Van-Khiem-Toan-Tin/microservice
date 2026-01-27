package com.ecommerce.inventoryservice.dto.request;

import com.ecommerce.inventoryservice.entity.InventoryType;
import lombok.Data;

import java.util.List;

@Data
public class InventoryAdjustRequest {

    private String skuCode;
    private InventoryType type; // IMPORT | EXPORT | ADJUST
    private Integer quantity;   // Dùng chủ yếu cho loại ADJUST
    private List<String> serialNumbers; // Dùng cho IMPORT và EXPORT
    private String note;
}
