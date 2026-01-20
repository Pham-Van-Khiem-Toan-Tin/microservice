package com.ecommerce.orderservice.event;

import com.ecommerce.orderservice.dto.event.OutboxMessage;
import com.ecommerce.orderservice.repository.OrderItemRepository;
import com.ecommerce.orderservice.repository.ProcessedEventRepository;
import com.ecommerce.orderservice.service.ProcessedEventStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@Slf4j
public class OrderReviewConsumer {
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OrderItemRepository orderItemRepository;
    // Giả sử bạn cũng có một store để chống trùng lặp event
    @Autowired
    private ProcessedEventStore processedEventStore;

    @KafkaListener(topics = "ecommerce.ecommerce.outbox_events", groupId = "order-service")
    public void onMessage(String value, Acknowledgment ack) {
        try {
            log.info("📥 Nhận sự kiện Review: {}", value);

            // 1) Parse root an toàn (Xử lý double-string/escaped)
            JsonNode root = objectMapper.readTree(value);
            if (root.isTextual()) {
                root = objectMapper.readTree(root.asText());
            }

            // 2) Bóc tách trường 'after' (Vì Debezium Mongo gửi data dạng String)
            String afterString = root.path("payload").path("after").asText();
            if (afterString == null || afterString.isEmpty()) {
                ack.acknowledge();
                return;
            }
            JsonNode doc = objectMapper.readTree(afterString);

            // 3) BÓC TÁCH EVENT ID để chống trùng lặp (Xử lý $oid)
            String eventId = doc.path("_id").path("$oid").asText();
            if (eventId.isEmpty()) {
                eventId = doc.path("_id").asText(); // Phòng hờ trường hợp ID phẳng
            }

            // 4) Map ra OutboxMessage để lấy eventType và payloadJson
            OutboxMessage msg = objectMapper.treeToValue(doc, OutboxMessage.class);

            // 5) Chỉ hứng sự kiện REVIEW_CREATED
            if (msg == null || !"REVIEW_CREATED".equals(msg.getEventType())) {
                ack.acknowledge();
                return;
            }

            // 6) Chống trùng lặp (Idempotency)
            if (!processedEventStore.tryMarkProcessed(eventId)) {
                log.info("⏭️ Event {} đã xử lý, bỏ qua.", eventId);
                ack.acknowledge();
                return;
            }

            // 7) Parse payloadJson để lấy dữ liệu nghiệp vụ
            JsonNode payload = objectMapper.readTree(msg.getPayloadJson());
            String orderItemId = payload.get("orderItemId").asText();

            // 8) TRẢ LẠI HÀM UPDATE REPO CỦA BẠN
            // Lưu ý: Chuỗi ID từ Mongo (24 ký tự) sẽ làm UUID.fromString() văng lỗi
            // nếu cột ID trong SQL của bạn thực sự là kiểu UUID.
            orderItemRepository.findById(UUID.fromString(orderItemId)).ifPresent(item -> {
                item.setReviewed(true);
                orderItemRepository.save(item);
                log.info("✅ Đã cập nhật reviewed cho Item {}", orderItemId);
            });

            ack.acknowledge();

        } catch (Exception e) {
            log.error("❌ Lỗi xử lý Review: {}", e.getMessage());
        }
    }
}
