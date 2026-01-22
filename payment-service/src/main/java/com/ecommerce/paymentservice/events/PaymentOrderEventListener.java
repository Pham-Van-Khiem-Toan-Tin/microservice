package com.ecommerce.paymentservice.events;

import com.ecommerce.paymentservice.dto.event.PaymentInitiateRequestedPayload;
import com.ecommerce.paymentservice.service.PaymentSagaService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.kafka.clients.consumer.AcknowledgeType;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.header.Header;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;

@Component
public class PaymentOrderEventListener {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private PaymentSagaService  paymentSagaService;
    @KafkaListener(topics = "order-service.payment.events", groupId = "payment-service")
    public void onPaymentEvent(ConsumerRecord<String, String> record, Acknowledgment acknowledgment) throws Exception {
        String eventType = header(record, "eventType");
        if (!"Payment.InitiateRequested".equals(eventType)) return;
        objectMapper.registerModule(new JavaTimeModule());
        String raw = record.value().trim();
        String json = raw.startsWith("\"") ? objectMapper.readValue(raw, String.class) : raw;
        PaymentInitiateRequestedPayload payload =
                objectMapper.readValue(json, PaymentInitiateRequestedPayload.class);

        paymentSagaService.handleInitiateRequested(payload);
        acknowledgment.acknowledge();
    }

    private String header(ConsumerRecord<String, String> record, String key) {
        Header h = record.headers().lastHeader(key);
        return h == null ? null : new String(h.value(), StandardCharsets.UTF_8);
    }
}
