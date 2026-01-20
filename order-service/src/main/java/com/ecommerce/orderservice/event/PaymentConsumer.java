package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.dto.event.PaymentSuccessEvent;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderStatisticsService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PaymentConsumer {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderStatisticsService  orderStatisticsService;

    @KafkaListener(topics = "payment-service.payment.events", groupId = "order-payment-group")
    public void handlePaymentSuccess(String message, Acknowledgment acknowledgment) {
        try {
            // 1. Xử lý lỗi "double-string" JSON từ Debezium (nếu có)
            String cleanJson = message;
            if (message.startsWith("\"") && message.endsWith("\"")) {
                cleanJson = objectMapper.readValue(message, String.class);
            }

            // 2. Parse JSON về DTO PaymentSuccessEvent
            PaymentSuccessEvent event = objectMapper.readValue(cleanJson, PaymentSuccessEvent.class);

            log.info("Nhận thông báo thanh toán thành công cho đơn hàng: {}", event.getOrderNumber());

            // 3. Tìm đơn hàng
            OrderEntity order = orderRepository.findByOrderNumber(event.getOrderNumber())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy đơn hàng: " + event.getOrderNumber()));

            // 4. KIỂM TRA TÍNH DUY NHẤT (Idempotency)
            if ("PAID".equals(order.getStatus().name())) {
                log.warn("Đơn hàng {} đã được cập nhật thanh toán trước đó. Bỏ qua.", event.getOrderNumber());
                return;
            }
            // 5. Cập nhật trạng thái đơn hàng
            order.setStatus(OrderStatus.CONFIRMED);
            order.setPaymentStatus(event.getPaymentStatus());
            order.setPaidAt(event.getPaidAt());
            // Có thể lưu thêm mã giao dịch để đối soát
            String tx = event.getTransactionNo();
            String marker = "GD VNPAY: " + tx;

            String cur = order.getNote();
            if (cur == null || !cur.contains(marker)) {
                order.setNote((cur == null || cur.isBlank()) ? marker : (cur + " | " + marker));
            }

            OrderEntity saved = orderRepository.save(order);
            orderStatisticsService.updateStatsOnNewOrder(saved);
            acknowledgment.acknowledge();
            log.info("Đơn hàng {} đã chuyển sang trạng thái PAID thành công.", event.getOrderNumber());

            // 6. (Tùy chọn) Kích hoạt các bước tiếp theo
            // Ví dụ: Gửi Email cho khách, báo cho bên Vận chuyển (Shipping Service)

        } catch (Exception e) {
            log.error("Lỗi khi xử lý sự kiện thanh toán từ Kafka: {}", e.getMessage());
            // Có thể ném lỗi để Kafka retry hoặc đẩy vào Dead Letter Topic
        }
    }
}
