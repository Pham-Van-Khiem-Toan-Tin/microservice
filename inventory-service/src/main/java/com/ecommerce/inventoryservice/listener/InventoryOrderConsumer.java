package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.dto.event.OrderEventPayload;
import com.ecommerce.inventoryservice.dto.event.StockUpdatePayload;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryOrderConsumer {
    @Autowired
    private InventoryService inventoryService;
    @Autowired
    private ObjectMapper objectMapper;
    @KafkaListener(topics = "order-service.order.events", groupId = "inventory-group")
    public void handleInventory(String message, Acknowledgment ack) {
        try {
            log.info("Nhận tin nhắn sạch: {}", message);
            String jsonContent = message;

            // 1. Xử lý double-string (rất quan trọng với tin nhắn sạch)
            if (message.startsWith("\"") && message.endsWith("\"")) {
                jsonContent = objectMapper.readValue(message, String.class);
            }

            JsonNode root = objectMapper.readTree(jsonContent);

            // 2. Dùng logic "Duck Typing" để phân loại sự kiện
            if (root.has("action")) {
                // Đây là sự kiện Admin chỉnh sửa kho (vì có trường action)
                StockUpdatePayload adjPayload = objectMapper.treeToValue(root, StockUpdatePayload.class);
                processAdminAdjustment(adjPayload);
            }
            else if (root.has("paymentMethod")) {
                // Đây là sự kiện Tạo đơn hàng mới (vì có paymentMethod)
                OrderEventPayload createPayload = objectMapper.treeToValue(root, OrderEventPayload.class);
                inventoryService.reserveStock(createPayload);
            }

            // 1. Sửa convertValue thành readValue để đọc chuỗi JSON

            // 2. Gọi Service để xử lý giữ kho
            ack.acknowledge();

        } catch (JsonProcessingException e) {
            log.error("Lỗi parse JSON từ Kafka: {}", e.getMessage());
            // Có thể đẩy vào Dead Letter Topic nếu cần
        } catch (Exception e) {
            log.error("Lỗi nghiệp vụ khi xử lý kho: {}", e.getMessage());
            // Ném lỗi để Kafka có thể retry nếu logic của bạn cho phép
            throw e;
        }
    }
    private void processAdminAdjustment(StockUpdatePayload payload) {
        if ("RESTORE_STOCK".equals(payload.getAction())) {
            inventoryService.restoreStock(payload.getItems());
            log.info("Admin hủy đơn: Đã hoàn kho cho đơn hàng {}", payload.getOrderNo());
        }
        else if ("CONFIRM_DEDUCTION".equals(payload.getAction())) {
            inventoryService.finalizeDeduction(payload.getOrderId(), payload.getItems());
            log.info("Giao hàng thành công: Đã xác nhận trừ kho cho đơn hàng {}", payload.getOrderNo());
        }
    }
}
