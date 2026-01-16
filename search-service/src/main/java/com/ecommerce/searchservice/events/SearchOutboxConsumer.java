package com.ecommerce.searchservice.events;

import com.ecommerce.searchservice.dto.OutboxMessage;
import com.ecommerce.searchservice.service.ElasticsearchProductService;
import com.ecommerce.searchservice.service.ProcessedEventStore;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
@Slf4j
public class SearchOutboxConsumer {

    private final ObjectMapper objectMapper;
    private final ElasticsearchProductService elasticService;
    private final ProcessedEventStore processedEventStore;

    @Value("${app.outbox-topic}")
    private String topic;

    @KafkaListener(topics = "ecommerce.ecommerce.outbox_events", groupId = "search-service")
    public void onMessage(String value, Acknowledgment ack) {
        try {
            // 1) Parse message an toàn
            JsonNode root = objectMapper.readTree(value);

            // Nếu message là "string JSON"
            if (root.isTextual()) {
                root = objectMapper.readTree(root.asText());
            }

            // 2) Nếu còn Debezium envelope -> lấy after/fullDocument
            JsonNode doc = root;
            JsonNode after = root.path("payload").path("after");
            if (!after.isMissingNode() && !after.isNull()) {
                doc = after.has("fullDocument") ? after.path("fullDocument") : after;
                if (doc.isTextual()) doc = objectMapper.readTree(doc.asText());
            }

            // 3) Map ra OutboxMessage
            OutboxMessage msg = objectMapper.treeToValue(doc, OutboxMessage.class);

            if (msg == null || msg.getEventType() == null) {
                log.warn("Skip: cannot parse OutboxMessage. doc={}", doc);
                ack.acknowledge();
                return;
            }
            Set<String> allowed = Set.of(
                    "PRODUCT_CREATED", "PRODUCT_INSERT", "PRODUCT_UPSERT", "PRODUCT_UPDATED",
                    "BRAND_UPSERT", "CATEGORY_UPSERT"
            );
            // ✅ ĐỔI cho khớp eventType bạn đang lưu ở catalog
            // Nếu catalog lưu "PRODUCT_CREATED" thì để vậy
            if (!allowed.contains(msg.getEventType())) {
                ack.acknowledge();
                return;
            }

            // 4) Idempotent theo _id
            String eventId = msg.eventId();
            if (eventId == null || !processedEventStore.tryMarkProcessed(eventId)) {
                ack.acknowledge();
                return;
            }

            // 5) Parse payloadJson
            String payloadJson = msg.getPayloadJson();
            if (payloadJson == null || payloadJson.isBlank()) {
                log.warn("payloadJson null/blank. msg={}", msg);
                ack.acknowledge();
                return;
            }

            Map<String, Object> payload = objectMapper.readValue(
                    payloadJson,
                    new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {}
            );

            switch (msg.getEventType()) {
                case "BRAND_UPSERT" -> {
                    String brandId = (String) payload.get("brandId");
                    String brandName = (String) payload.get("name");
                    String brandSlug = (String) payload.get("slug");
                    if (brandId == null || brandName == null || brandSlug == null) {
                        log.warn("Invalid brand payload: {}", payload);
                        ack.acknowledge();
                        return;
                    }

                    elasticService.updateBrandInProducts(brandId, brandName, brandSlug);
                }
                case "CATEGORY_UPSERT" -> {
                    String categoryId = (String) payload.get("id");
                    String name = (String) payload.get("name");
                    String slug = (String) payload.get("slug");

                    List<String> ancestorIds = null;
                    Object raw = payload.get("ancestorIds");
                    if (raw instanceof List<?> l) {
                        ancestorIds = l.stream().map(String::valueOf).toList();
                    }

                    if (categoryId == null || categoryId.isBlank()) {
                        ack.acknowledge();
                        return;
                    }

                    elasticService.updateCategoryInProducts(categoryId, name, slug, ancestorIds);
                }
                default -> { // product events
                    String productId = (String) payload.get("productId");
                    if (productId == null || productId.isBlank()) {
                        log.warn("product payload missing productId. payload={}", payload);
                        ack.acknowledge();
                        return;
                    }
                    elasticService.upsertProduct(productId, payload);
                }
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ Search xử lý outbox lỗi (no ack => retry)", e);
            // không ack để retry
        }
    }

}

