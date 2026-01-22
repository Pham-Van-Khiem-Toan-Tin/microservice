package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.dto.event.OrderEventPayload;
import com.ecommerce.orderservice.dto.event.ReviewCreatedPayload;
import com.ecommerce.orderservice.dto.event.StockResultPayload;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.entity.CartEntity;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentMethod;
import com.ecommerce.orderservice.repository.CartItemRepository;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.repository.OrderItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.ecommerce.orderservice.constants.Constants.CART_NOT_FOUND;

@Component
@Slf4j
public class OrderServiceConsumer {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @KafkaListener(topics = "product-review-events", groupId = "order-service-group")
    public void handleReviewCreated(String message, Acknowledgment acknowledgment) {
        try {
            // 1. Xử lý "double-string" JSON (Phòng trường hợp Outbox/Kafka bọc thêm 1 lớp nháy)
            String cleanJson = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                cleanJson = objectMapper.readValue(message, String.class);
            }

            // 2. Parse về Event chung của Outbox
            JsonNode eventNode = objectMapper.readTree(cleanJson);
            String eventType = eventNode.get("eventType").asText();

            // 3. Chỉ xử lý nếu đúng là loại sự kiện mong muốn
            if ("REVIEW_CREATED".equals(eventType)) {
                // Parse payloadJson bên trong
                String payloadStr = eventNode.get("payloadJson").asText();
                // Map về DTO (orderItemId, orderId, isReviewed)
                ReviewCreatedPayload payload = objectMapper.readValue(payloadStr, ReviewCreatedPayload.class);

                log.info("Nhận được đánh giá cho sản phẩm trong đơn hàng: {}", payload.getOrderId());

                // 4. Tìm OrderItem cần cập nhật
                orderItemRepository.findById(UUID.fromString(payload.getOrderItemId())).ifPresent(item -> {
                    // Cập nhật trạng thái reviewed (không sợ mất tiền tố is)
                    item.setReviewed(true);
                    orderItemRepository.save(item);
                    log.info("Đã cập nhật trạng thái đã đánh giá cho ItemId: {}", payload.getOrderItemId());
                });
            }

            // 5. Xác nhận đã xử lý xong tin nhắn
            acknowledgment.acknowledge();

        } catch (Exception e) {
            log.error("Lỗi khi xử lý Review Event cho Order Service: {}", e.getMessage());
            // Tùy chọn: Không acknowledge để Kafka retry hoặc đẩy vào DLT
        }
    }
    @KafkaListener(topics = "inventory-service.stock.events", groupId = "order-service-group")
    public void handleStockResult(String message, Acknowledgment acknowledgment) {
        try {
            // 1. Xử lý "double-string" JSON
            String cleanJson = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                cleanJson = objectMapper.readValue(message, String.class);
            }

            // 2. Parse về DTO StockResultPayload
            StockResultPayload event = objectMapper.readValue(cleanJson, StockResultPayload.class);

            // 3. Tìm đơn hàng cần xử lý
            OrderEntity order = orderRepository.findById(UUID.fromString(event.getOrderId()))
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + event.getOrderId()));
            if (order.getStatus() == OrderStatus.CANCELLED
                    || order.getStatus() == OrderStatus.EXPIRED
                    || order.getStatus() == OrderStatus.COMPLETED) {
                acknowledgment.acknowledge();
                return;
            }
            // 4. Cập nhật trạng thái dựa trên kết quả từ kho
            if ("SUCCESS".equals(event.getStatus())) {
                log.info("Kho xác nhận thành công đơn hàng: {}", event.getOrderId());
                // Chuyển sang CONFIRMED (Chờ thanh toán hoặc Chờ giao hàng)
                if (!order.getPaymentMethod().equals(PaymentMethod.COD)) {
                    order.setStatus(OrderStatus.RESERVED);
                } else {
                    order.setStatus(OrderStatus.CONFIRMED);
                }
            } else {
                log.warn("Kho xác nhận thất bại đơn hàng: {}. Lý do: {}", event.getOrderId(), event.getReason());
                // Thực hiện ROLLBACK trạng thái đơn hàng
                order.setStatus(OrderStatus.CANCELLED);
            }

            orderRepository.save(order);
            acknowledgment.acknowledge();
            log.info("Đã cập nhật trạng thái đơn hàng {} thành {}", event.getOrderId(), order.getStatus());

        } catch (Exception e) {
            log.error("Lỗi khi xử lý phản hồi từ kho cho Order Service: {}", e.getMessage());
        }
    }
    @KafkaListener(topics = "inventory-service.stock.events", groupId = "cart-group")
    public void handleStockResponse(String message, Acknowledgment acknowledgment) {
        try {
            // Xử lý double-string từ Debezium
            String cleanJson = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                cleanJson = objectMapper.readValue(message, String.class);
            }

            StockResultPayload result = objectMapper.readValue(cleanJson, StockResultPayload.class);

            if ("SUCCESS".equals(result.getStatus())) {
                // Chỉ xóa giỏ hàng khi kho đã giữ hàng thành công
                cartItemRepository.deleteByUserId(result.getUserId());
                acknowledgment.acknowledge();
                log.info("Đã dọn dẹp giỏ hàng cho user: {}", result.getUserId());
            }
        } catch (Exception e) {
            log.error("Lỗi parse StockResultPayload: {}", e.getMessage());
        }
    }
}
