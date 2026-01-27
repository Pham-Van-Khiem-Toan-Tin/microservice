package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.dto.event.PaymentFailedResult;
import com.ecommerce.inventoryservice.dto.event.PaymentSuccessResult;
import com.ecommerce.inventoryservice.handler.InventoryHandler;
import com.ecommerce.inventoryservice.repository.InventoryItemRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class InventoryPaymentConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InventoryHandler inventoryHandler;
    @Autowired
    private InventoryItemRepository inventoryItemRepo;

    @Transactional
    @KafkaListener(topics = "payment-service.payment.events", groupId = "inventory-group")
    public void handlePaymentSuccess(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            String eventType = header(record, "eventType");
            objectMapper.registerModule(new JavaTimeModule());
            String raw = record.value().trim();
            String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
            String orderNo = peekOrderNumber(raw);
            MDC.put("orderNo", orderNo);
            MDC.put("sagaStep", "INVENTORY_PAYMENT_CONSUMER");
            MDC.put("status", "STARTED");
            if (eventType == null) return;
            log.info("Nhận tin thanh toán từ Kafka. Event: {}", eventType);
            PaymentSuccessResult payload =
                    objectMapper.readValue(json, PaymentSuccessResult.class);
            inventoryHandler.handlePaySucceeded(payload);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            MDC.put("status", "FAILED");
            log.error("Lỗi xử lý chốt kho: {}", e.getMessage());
            // Có thể ném lỗi để Kafka retry hoặc đẩy vào Dead Letter Topic
        } finally {
            MDC.clear();
        }
    }
    private String peekOrderNumber(String json) {
        if (json == null || json.isBlank()) return "UNKNOWN";

        // Tìm cặp "orderNumber":"..." hoặc "orderNo":"..."
        Pattern pattern = Pattern.compile("\"orderNumber\"\\s*:\\s*\"([^\"]+)\"");
        Matcher matcher = pattern.matcher(json);

        if (matcher.find()) {
            return matcher.group(1);
        }

        // Backup: Nếu bạn thỉnh thoảng dùng orderNo thay vì orderNumber
        Pattern backupPattern = Pattern.compile("\"orderNo\"\\s*:\\s*\"([^\"]+)\"");
        Matcher backupMatcher = backupPattern.matcher(json);

        return backupMatcher.find() ? backupMatcher.group(1) : "UNKNOWN";
    }
    private String header(ConsumerRecord<String, String> record, String key) {
        Header h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
