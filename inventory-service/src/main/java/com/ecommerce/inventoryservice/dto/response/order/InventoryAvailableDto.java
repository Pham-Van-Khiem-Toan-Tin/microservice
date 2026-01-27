package com.ecommerce.inventoryservice.dto.response.order;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class InventoryAvailableDto {
    private String skuCode;
    private List<String> serialNumbers;
    private boolean available;
}
