package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.dto.event.InventoryCommittedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReleasedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReserveFailedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReservedPayload;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.handler.OrderInventoryHandler;
import com.ecommerce.orderservice.handler.OrderInventoryResultHandler;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

@Component
@Slf4j
public class OrderInventoryConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderInventoryHandler orderInventoryHandler;
    @Autowired
    private OrderInventoryResultHandler handler;

    @KafkaListener(topics = "inventory-service.inventory.events", groupId = "order-inventory-group")
    @Transactional
    public void onInventoryEvent(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
        // 1) Lấy type từ header
        String eventType = header(record, "eventType");
        objectMapper.registerModule(new JavaTimeModule());
        String raw = record.value().trim();
        String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
        if (eventType == null) return;
        switch (eventType) {
            case "Inventory.Reserved" -> {
                InventoryReservedPayload payload =
                        objectMapper.readValue(json, InventoryReservedPayload.class);
                orderInventoryHandler.handleReserved(payload);
            }
            case "Inventory.ReserveFailed" -> {
                InventoryReserveFailedPayload payload =
                        objectMapper.readValue(json, InventoryReserveFailedPayload.class);
                orderInventoryHandler.handleReserveFailed(payload);
            }
            case "Inventory.Committed" -> {
                InventoryCommittedPayload payload =
                        objectMapper.readValue(json, InventoryCommittedPayload.class);
                handler.handleCommitted(payload);
            }
            case "Inventory.Released" -> {
                InventoryReleasedPayload payload =
                        objectMapper.readValue(json, InventoryReleasedPayload.class);
                handler.handleReleased(payload);
            }
            default -> {
                // ignore
            }
        }
        ack.acknowledge();
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        Header h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
