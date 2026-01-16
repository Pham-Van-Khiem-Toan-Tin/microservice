package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.entity.InventoryType;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    Page<InventoryDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    void createInventory(String skuCode, Integer initialStock);
    boolean reserveStock(String skuCode, Integer quantity);
    void confirmOrder(String skuCode, Integer quantity, String orderId);
    void logHistory(String sku, int change, int stockAfter, InventoryType type, String ref);
    InventoryAdjustResponse adjustInventory(InventoryAdjustRequest req);
}
