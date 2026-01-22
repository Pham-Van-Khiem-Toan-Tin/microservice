package com.ecommerce.orderservice.handler;

import com.ecommerce.orderservice.dto.event.InventoryCommittedPayload;
import com.ecommerce.orderservice.dto.event.InventoryReleasedPayload;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentMethod;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

import static com.ecommerce.orderservice.constants.Constants.VALIDATE_FAIL;

@Service
public class OrderInventoryResultHandler {
    @Autowired
    private OrderRepository orderRepository;
    @Transactional
    public void handleCommitted(InventoryCommittedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent: nếu đã CONFIRMED rồi thì bỏ qua
        if (order.getStatus() == OrderStatus.CONFIRMED
                || order.getStatus() == OrderStatus.SHIPPING
                || order.getStatus() == OrderStatus.DELIVERED
                || order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }

        // Chỉ cho phép commit khi đã PAID (online) hoặc CONFIRMED (COD tuỳ flow)
        // Flow chuẩn online: PAID -> CONFIRMED
        if (order.getStatus() == OrderStatus.PAID || order.getPaymentMethod() == PaymentMethod.COD) {
            order.setStatus(OrderStatus.CONFIRMED);
            order.setUpdatedAt(LocalDateTime.now()); // optional
        }

        orderRepository.save(order);
    }

    @Transactional
    public void handleReleased(InventoryReleasedPayload ev) {
        UUID orderId = UUID.fromString(ev.getOrderId());

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent: nếu đã cancelled/expired thì ok, chỉ update note nếu cần
        if (order.getStatus() != OrderStatus.CANCELLED && order.getStatus() != OrderStatus.EXPIRED) {
            // tùy policy: nếu release mà order chưa cancel thì bạn có thể set cancel luôn
            order.setStatus(OrderStatus.CANCELLED);
        }

        if (order.getCancelReason() == null) {
            order.setCancelReason(ev.getReason() != null ? ev.getReason() : "RELEASED");
        }

        orderRepository.save(order);
    }
}
