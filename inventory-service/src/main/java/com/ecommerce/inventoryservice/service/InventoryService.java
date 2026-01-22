package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.event.InventoryReserveRequestedPayload;
import com.ecommerce.inventoryservice.dto.event.OrderCreatedPayload;
import com.ecommerce.inventoryservice.dto.event.StockUpdatePayload;
import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.request.OrderItemCheckForm;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.dto.response.cart.InventoryCartDto;
import com.ecommerce.inventoryservice.entity.InventoryType;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;

import java.util.List;

public interface InventoryService {
    void releaseForOrder(InventoryReserveRequestedPayload p) throws JsonProcessingException;
    Page<InventoryDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    void createInventory(String skuCode, Integer initialStock);
    void reserveStock(OrderCreatedPayload payload) throws JsonProcessingException;
    void confirmOrder(String skuCode, Integer quantity, String orderId);
    void logHistory(String sku, int change, int stockAfter, InventoryType type, String ref);
    InventoryAdjustResponse adjustInventory(InventoryAdjustRequest req);
    List<InventoryCartDto> getStockOfSkus(List<String> skuCodes);
    boolean checkAvailability(List<OrderItemCheckForm> items);
    void restoreStock(List<StockUpdatePayload.StockItem> items);
    void finalizeDeduction(String orderId, List<StockUpdatePayload.StockItem> items);
}
