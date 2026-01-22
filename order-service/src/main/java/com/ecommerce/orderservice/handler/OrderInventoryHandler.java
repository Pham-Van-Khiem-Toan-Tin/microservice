package com.ecommerce.orderservice.handler;

import com.ecommerce.orderservice.dto.event.InventoryCommitRequestedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReserveFailedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReservedPayload;
import com.ecommerce.orderservice.dto.event.PaymentInitiateRequestedPayload;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.UUID;

import static com.ecommerce.orderservice.constants.Constants.VALIDATE_FAIL;

@Service
public class OrderInventoryHandler {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Transactional
    public void handleReserved(InventoryReservedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        if (order.getStatus() != OrderStatus.CREATED) {
            return;
        }
        order.setReservationId(ev.getReservationId());
        if (order.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.CONFIRMED);
            // Optional: emit Order.Confirmed để kho/ship bắt đầu xử lý

//            emitOrderEvent(order, "inventory", "Inventory.CommitRequested");
        } else {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
            emitPaymentInitiateRequested(order);
        }
        CartEntity cart = cartRepository.findByUserId(order.getUserId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        cart.getItems().clear();
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);
        orderRepository.save(order);
    }

    @Transactional
    public void handleReserveFailed(InventoryReserveFailedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent
        if (order.getStatus() == OrderStatus.CANCELLED || order.getStatus() == OrderStatus.EXPIRED) {
            return;
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelReason(ev.getReason() != null ? ev.getReason() : "OUT_OF_STOCK");



        orderRepository.save(order);
    }

    private void emitPaymentInitiateRequested(OrderEntity order) {
        try {
            PaymentInitiateRequestedPayload payload = PaymentInitiateRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .clientIp(order.getClientIp())
                    .amount(order.getFinalAmount().longValue())
                    .method(order.getPaymentMethod())
                    .build();

            OutboxEvent outbox = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("payment") // route sang payment-service.Payment.events
                    .aggregateId(order.getId().toString())
                    .type("Payment.InitiateRequested")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void emitOrderEvent(OrderEntity order, String topicName, String eventType) {
        try {
            InventoryCommitRequestedPayload payload = InventoryCommitRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .reservationId(order.getReservationId())
                    .build();

            OutboxEvent outbox = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType(topicName)
                    .aggregateId(order.getId().toString())
                    .type(eventType)
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
