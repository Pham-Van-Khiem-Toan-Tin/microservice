package com.ecommerce.orderservice.handler;

import com.ecommerce.orderservice.dto.event.*;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.dto.request.OrderCreateInventoryForm;
import com.ecommerce.orderservice.dto.request.OrderItemCheckForm;
import com.ecommerce.orderservice.dto.response.inventory.InventoryAvailableDto;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.integration.InventoryFeignClient;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.OrderStatisticsService;
import com.ecommerce.orderservice.service.SseService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    private SseService sseService;
    @Autowired
    private OrderStatisticsService orderStatisticsService;
    @Autowired
    private InventoryFeignClient inventoryFeignClient;
    @Autowired
    private CartRepository cartRepository;

    @Transactional
    public void handlePaySucceeded(PaymentSuccessResult ev) {
        MDC.put("orderNo", ev.getOrderNumber());
        MDC.put("sagaStep", "PAYMENT_SUCCESS_HANDLER");
        log.info("Bắt đầu xử lý logic thanh toán thành công nội bộ");
        OrderEntity order = orderRepository.findByOrderNumber(ev.getOrderNumber())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        // ✅ Idempotent: đã PAID rồi thì bỏ qua
        if (order.getPaymentStatus() == PaymentStatus.SUCCEEDED) {
            log.warn("Sự kiện trùng lặp (Idempotent): Đơn hàng đã ở trạng thái SUCCEEDED trước đó.");
            return;
        }

        // Đánh dấu đã nhận tiền
        order.setPaymentStatus(PaymentStatus.SUCCEEDED);
        order.setPaidAt(LocalDateTime.now());
        order.setPayProvider(ev.getProviderRef());

        boolean isLate =
                order.getStatus() == OrderStatus.EXPIRED
                        || order.getStatus() == OrderStatus.CANCELLED
                        || (order.getExpiresAt() != null && LocalDateTime.now().isAfter(order.getExpiresAt()));

        if (isLate) {
            MDC.put("paymentType", "LATE_PAYMENT");
            log.warn("Phát hiện thanh toán muộn cho đơn hàng #{}. Trạng thái hiện tại: {}", order.getOrderNumber(), order.getStatus());
            // ✅ Thanh toán muộn: KHÔNG commit kho
            // Giữ nguyên EXPIRED/CANCELLED để tránh “hồi sinh” đơn khi hàng đã release
            List<ReReserveRequest.ReReserveItem> checkFormList = order.getOrderItems().stream()
                    .map(it -> new ReReserveRequest.ReReserveItem(it.getSkuCode(), it.getQuantity()))
                    .collect(Collectors.toCollection(ArrayList::new));
            List<InventoryAvailableDto> inventoryResponses = new ArrayList<>();
            try {
                log.info("Đang gọi Inventory Service để 'hồi sinh' (re-reserve) hàng cho đơn thanh toán muộn...");
                inventoryResponses = inventoryFeignClient
                        .reReserve(ReReserveRequest.builder()
                                .items(checkFormList)
                                .orderId(order.getId().toString())
                                .build());
            } catch (Exception e) {
                log.error("Lỗi khi gọi Inventory re-reserve: {}", e.getMessage());
                throw new BusinessException(VALIDATE_FAIL);
            }
            if (inventoryResponses.stream().allMatch(InventoryAvailableDto::isAvailable)) {
                log.info("Hồi sinh hàng thành công! Kho đã cấp lại Serial mới.");
                order.setPaymentStatus(PaymentStatus.SUCCEEDED);
                CartEntity cart = cartRepository.findByUserId(order.getUserId())
                        .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
                cart.getItems().clear();
                cartRepository.save(cart);
            } else {
                MDC.put("status", "REFUND_REQUIRED");
                log.error("Hồi sinh thất bại do HẾT HÀNG. Đã ra lệnh hoàn tiền (Refund) cho khách.");
                order.setPaymentStatus(PaymentStatus.REFUNDED);
                emitWalletRefundRequested(order, ev);
            }
            order.setStatus(OrderStatus.PAID);
            // ✅ Hoàn ví nội bộ

            orderRepository.save(order);
            return;
        }

        // ✅ Thanh toán đúng hạn
        if (order.getStatus() == OrderStatus.AWAITING_PAYMENT || order.getStatus() == OrderStatus.RESERVED) {
            MDC.put("paymentType", "ON_TIME");
            log.info("Thanh toán đúng hạn. Đang chốt đơn và dọn dẹp giỏ hàng...");
            order.setStatus(OrderStatus.PAID);
            order.setPaymentStatus(PaymentStatus.SUCCEEDED);
            CartEntity cart = cartRepository.findByUserId(order.getUserId())
                    .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
            cart.getItems().clear();
            cartRepository.save(cart);
//            emitPayCommitRequested(order);
        }

        OrderEntity saved = orderRepository.save(order);
        orderStatisticsService.updateStatsOnNewOrder(saved);
        sseService.sendPaymentSuccess(order.getOrderNumber());
        log.info("Hoàn tất xử lý thanh toán thành công. Trạng thái cuối: Order={}, Payment={}",
                saved.getStatus(), saved.getPaymentStatus());
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

        }

        orderRepository.save(order);
    }


}
