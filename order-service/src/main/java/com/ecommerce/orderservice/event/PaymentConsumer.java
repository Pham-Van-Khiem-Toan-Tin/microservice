package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.dto.event.*;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.handler.OrderPaymentHandler;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.OrderStatisticsService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class PaymentConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderPaymentHandler  orderPaymentHandler;
    @KafkaListener(topics = "payment-service.payment.events", groupId = "order-payment-group")
    public void handlePaymentSuccess(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws JsonProcessingException {
        try {
            String eventType = header(record, "eventType");
            if (eventType == null) return;
            objectMapper.registerModule(new JavaTimeModule());
            String raw = record.value().trim();
            String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
            String orderNo = peekOrderNumber(raw);
            MDC.put("orderNo", orderNo);
            MDC.put("sagaStep", "KAFKA_PAYMENT_CONSUMER");
            MDC.put("status", "STARTED");
            log.info("Nhận sự kiện Kafka: {} | Payload: {}", eventType, raw);
            switch (eventType) {
                case "Payment.Succeeded" -> {
                    PaymentSuccessResult payload =
                            objectMapper.readValue(json, PaymentSuccessResult.class);
                    orderPaymentHandler.handlePaySucceeded(payload);
                }
                case "Payment.Failed" -> {
                    PaymentFailedResult payload =
                            objectMapper.readValue(json, PaymentFailedResult.class);
                    orderPaymentHandler.handleFailed(payload);
                }
                default -> {
                    // ignore
                }
            }
            MDC.put("status", "SUCCESS");
            log.info("Xử lý sự kiện {} thành công và đã Ack", eventType);
            acknowledgment.acknowledge();
        } catch (Exception e) {
            MDC.put("status", "FAILED");
            log.error("Thảm họa xử lý Kafka Payment: {}", e.getMessage());
            throw e;
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
