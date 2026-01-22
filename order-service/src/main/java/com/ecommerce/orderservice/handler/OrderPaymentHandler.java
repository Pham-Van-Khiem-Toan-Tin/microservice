package com.ecommerce.orderservice.handler;

import com.ecommerce.orderservice.dto.event.*;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.ecommerce.orderservice.constants.Constants.VALIDATE_FAIL;

@Service
@Slf4j
public class OrderPaymentHandler {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private SseService  sseService;
    @Transactional
    public void handleInitiated(PaymentInitiatedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent: nếu đã có paymentId/url thì bỏ qua
        if (order.getPaymentId() != null && order.getPaymentUrl() != null) {
            return;
        }

        order.setPaymentId(ev.getPaymentId());
        order.setPaymentUrl(ev.getPaymentUrl());
        order.setPaymentStatus(PaymentStatus.PENDING);

        // giữ orderStatus ở AWAITING_PAYMENT (đúng flow)
        if (order.getStatus() == OrderStatus.RESERVED) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
        }

        orderRepository.save(order);
        sseService.sendPaymentUrl(order.getOrderNumber(), order.getPaymentUrl(), order.getExpiresAt());
    }
    @Transactional
    public void handlePaySucceeded(PaymentSuccessResult ev) {

        OrderEntity order = orderRepository.findByOrderNumber(ev.getOrderNumber())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent: đã PAID rồi thì bỏ qua
        if (order.getPaymentStatus() == PaymentStatus.SUCCEEDED) return;

        // Đánh dấu đã nhận tiền
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        order.setPaidAt(LocalDateTime.now());
        order.setPayProvider(ev.getProviderRef());

        boolean isLate =
                order.getStatus() == OrderStatus.EXPIRED
                        || order.getStatus() == OrderStatus.CANCELLED
                        || (order.getExpiresAt() != null && LocalDateTime.now().isAfter(order.getExpiresAt()));

        if (isLate) {
            // ✅ Thanh toán muộn: KHÔNG commit kho
            // Giữ nguyên EXPIRED/CANCELLED để tránh “hồi sinh” đơn khi hàng đã release
            if (order.getStatus() != OrderStatus.EXPIRED && order.getStatus() != OrderStatus.CANCELLED) {
                order.setStatus(OrderStatus.EXPIRED);
            }

            // ✅ Hoàn ví nội bộ
            emitWalletRefundRequested(order, ev);

            orderRepository.save(order);
            return;
        }

        // ✅ Thanh toán đúng hạn
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT || order.getStatus() == OrderStatus.RESERVED) {
            order.setStatus(OrderStatus.PAID);
            emitPayCommitRequested(order);
        }

        orderRepository.save(order);
        sseService.sendPaymentSuccess(order.getOrderNumber());
    }
    private void emitPayCommitRequested(OrderEntity order) {
        try {
            InventoryCommitRequestedPayload payload = InventoryCommitRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .reservationId(order.getReservationId())
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")                 // -> order-service.Inventory.events
                    .aggregateId(order.getId().toString())
                    .type("Inventory.CommitRequested")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void emitWalletRefundRequested(OrderEntity order, PaymentSuccessResult ev) {
        try {
            WalletRefundRequestedPayload payload = WalletRefundRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .orderNumber(order.getOrderNumber())
                    .userId(order.getUserId())
                    .amount(order.getFinalAmount().longValue())
                    .reason("LATE_PAYMENT")
                    .providerRef(ev.getProviderRef())
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("wallet")                    // bạn route sang wallet-service
                    .aggregateId(order.getId().toString())
                    .type("Wallet.RefundRequested")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    @Transactional
    public void handleInitialFailed(PaymentFailedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // Idempotent
        if (order.getPaymentStatus() == PaymentStatus.SUCCEEDED) return;

        // Chỉ ghi nhận lỗi, KHÔNG cancel
        order.setPaymentStatus(PaymentStatus.FAILED);

        // Giữ order ở AWAITING_PAYMENT để retry
        if (order.getStatus() == OrderStatus.RESERVED) {
            order.setStatus(OrderStatus.AWAITING_PAYMENT);
        }

        orderRepository.save(order);
    }
    @Transactional
    public void handleFailed(PaymentFailedResult ev) {

        OrderEntity order = orderRepository.findByOrderNumber(ev.getOrderNumber())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // Nếu đã paid rồi mà lại nhận failed (out-of-order) -> ignore
        if (order.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Ignore Payment.Failed because order already PAID. orderId={}", order.getId());
            return;
        }

        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setUpdatedAt(ev.getFailedAt());

        // 3️⃣ Xử lý trạng thái order
        // Nếu vẫn đang chờ thanh toán → cancel
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT
                || order.getStatus() == OrderStatus.RESERVED) {

            order.setStatus(OrderStatus.CANCELLED);
            order.setCancelReason("PAYMENT_FAILED: " + ev.getReason());

            // 4️⃣ Release kho nếu đã từng reserve
            if (order.getReservationId() != null) {
                emitInventoryReleaseRequested(order);
            }
        }

        orderRepository.save(order);
    }
    private void emitInventoryCommitRequested(OrderEntity order) {
        try {
            InventoryCommitRequestedPayload payload = InventoryCommitRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .reservationId(order.getReservationId())
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(order.getId().toString())
                    .type("Inventory.CommitRequested")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void emitInventoryReleaseRequested(OrderEntity order) {
        try {
            InventoryReleaseRequestedPayload payload = InventoryReleaseRequestedPayload.builder()
                    .orderId(order.getId().toString())
                    .reservationId(order.getReservationId())
                    .reason(order.getCancelReason())
                    .build();

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(order.getId().toString())
                    .type("Inventory.ReleaseRequested")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void emitOrderEvent(OrderEntity order, String eventType) {
        try {
            String payloadJson = objectMapper.writeValueAsString(
                    java.util.Map.of("orderId", order.getId().toString(), "orderNumber", order.getOrderNumber())
            );

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("Order")
                    .aggregateId(order.getId().toString())
                    .type(eventType)
                    .payload(payloadJson)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
