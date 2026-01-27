package com.ecommerce.orderservice.dto.response.inventory;

import lombok.Data;

import java.util.List;

@Data
public class InventoryAvailableDto {
    private String skuCode;
    private List<String> serialNumbers;
    private boolean available;
}
