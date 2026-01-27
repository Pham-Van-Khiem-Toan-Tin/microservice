//package com.ecommerce.inventoryservice.listener;
//
//import com.ecommerce.inventoryservice.dto.event.*;
//import com.ecommerce.inventoryservice.service.InventoryService;
//import com.ecommerce.inventoryservice.service.impl.InventoryReservationService;
//import com.fasterxml.jackson.core.JsonProcessingException;
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
//import lombok.extern.slf4j.Slf4j;
//import org.apache.kafka.clients.consumer.ConsumerRecord;
//import org.apache.kafka.common.header.Header;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.kafka.support.Acknowledgment;
//import org.springframework.stereotype.Component;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.nio.charset.StandardCharsets;
//
//@Component
//@Slf4j
//public class InventoryOrderConsumer {
//    @Autowired
//    private InventoryService inventoryService;
//    @Autowired
//    private ObjectMapper objectMapper;
//    @Autowired
//    private InventoryReservationService inventoryReservationService;
//
////    @KafkaListener(topics = "order-service.order.events", groupId = "inventory-group")
////    public void handleInventory(ConsumerRecord<String, String> record, Acknowledgment ack) throws Exception {
////            String eventType = readHeader(record, "eventType");
////            if (!eventType.equals("Order.Create")) {
////                ack.acknowledge();
////                return;
////            }
////            objectMapper.registerModule(new JavaTimeModule());
////            String message = record.value();
////            log.info("Nhận tin nhắn sạch: {}", message);
////            String jsonContent = message;
////            objectMapper.registerModule(new JavaTimeModule());
////            // 1. Xử lý double-string (rất quan trọng với tin nhắn sạch)
////            if (message.startsWith("\"") && message.endsWith("\"")) {
////                jsonContent = objectMapper.readValue(message, String.class);
////            }
////            OrderCreatedPayload payload = objectMapper.readValue(jsonContent, OrderCreatedPayload.class);
////            inventoryService.reserveStock(payload);
////            ack.acknowledge();
////
////
////    }
//    private String readHeader(ConsumerRecord<String, String> record, String key) {
//        Header h = record.headers().lastHeader(key);
//        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
//    }
////    @Transactional
////    @KafkaListener(topics = "order-service.inventory.events", groupId = "inventory-group")
////    public void onOrderInventoryEvent(ConsumerRecord<String, String> record) throws Exception {
////
////        String eventType = header(record, "eventType");
////        if (eventType == null) return;
////        String raw = record.value().trim();
////        String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
////        switch (eventType) {
////            case "Inventory.CommitRequested" -> {
////                InventoryCommitRequestedPayload payload =
////                        objectMapper.readValue(json, InventoryCommitRequestedPayload.class);
////                inventoryReservationService.commitReservation(payload);
////            }
////            case "Inventory.ReleaseRequested" -> {
////                InventoryReleaseRequestedPayload payload =
////                        objectMapper.readValue(json, InventoryReleaseRequestedPayload.class);
////                inventoryReservationService.releaseReservation(payload);
////            }
////            default -> {
////                // ignore
////            }
////        }
////    }
//
//    private String header(ConsumerRecord<String, String> record, String key) {
//        Header h = record.headers().lastHeader(key);
//        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
//    }
//}
