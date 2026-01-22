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
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@Slf4j
public class PaymentConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderPaymentHandler  orderPaymentHandler;
    @Transactional
    @KafkaListener(topics = "payment-service.payment.events", groupId = "order-payment-group")
    public void handlePaymentSuccess(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) {
        try {
            String eventType = header(record, "eventType");
            if (eventType == null) return;
            objectMapper.registerModule(new JavaTimeModule());
            String raw = record.value().trim();
            String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
            switch (eventType) {
                case "Payment.Initiated" -> {
                    PaymentInitiatedPayload payload =
                            objectMapper.readValue(json, PaymentInitiatedPayload.class);
                    orderPaymentHandler.handleInitiated(payload);
                }
                case "Payment.Succeeded" -> {
                    PaymentSuccessResult payload =
                            objectMapper.readValue(json, PaymentSuccessResult.class);
                    orderPaymentHandler.handlePaySucceeded(payload);
                }
                case "Payment.InitiateFailed" -> {
                    PaymentFailedPayload payload =
                            objectMapper.readValue(json, PaymentFailedPayload.class);
                    orderPaymentHandler.handleInitialFailed(payload);
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
            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện thanh toán từ Kafka: {}", e.getMessage());
            // Có thể ném lỗi để Kafka retry hoặc đẩy vào Dead Letter Topic
        }
    }
    private String header(ConsumerRecord<String, String> record, String key) {
        Header h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
