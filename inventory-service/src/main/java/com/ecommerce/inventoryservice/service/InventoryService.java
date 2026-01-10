package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.entity.InventoryType;

public interface InventoryService {
    void createInventory(String skuCode, Integer initialStock);
    boolean reserveStock(String skuCode, Integer quantity);
    void confirmOrder(String skuCode, Integer quantity, String orderId);
    void logHistory(String sku, int change, int stockAfter, InventoryType type, String ref);
}
