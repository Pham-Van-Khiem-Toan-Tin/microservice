package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.dto.event.ProductCreatedEvent;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryEventListener {
    @Autowired
    InventoryService inventoryService;
    @Autowired
    ObjectMapper objectMapper;

    @KafkaListener(topics = "catalog.product.created", groupId = "inventory-group-test-final-1")
    public void handleProductCreated(String message) {
        log.info("📩 KAFKA RAW MESSAGE: {}", message);

        try {
            // 👇 Tự tay biến String thành Object tại đây
            ProductCreatedEvent event = objectMapper.readValue(message, ProductCreatedEvent.class);

            log.info("✅ Parse OK. ProductID: {}", event.getProductId());

            if (event.getSkus() != null) {
                for (ProductCreatedEvent.SkuInitData sku : event.getSkus()) {
                    inventoryService.createInventory(sku.getSkuCode(), sku.getInitialStock());
                }
            }
        } catch (Exception e) {
            log.error("❌ Lỗi xử lý event: {}", e.getMessage());
        }
    }

}
