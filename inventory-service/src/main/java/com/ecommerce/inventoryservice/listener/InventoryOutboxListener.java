package com.ecommerce.inventoryservice.listener;

import com.ecommerce.inventoryservice.dto.event.OutboxMessage;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.ecommerce.inventoryservice.service.impl.ProcessedEventStore;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class InventoryOutboxListener {

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    private final ProcessedEventStore processedEventStore;

    @KafkaListener(topics = "ecommerce.ecommerce.outbox_events", groupId = "inventory-service")
    public void handleOutbox(String message, Acknowledgment ack) {
        try {
            // 1) parse lần 1
            JsonNode root = objectMapper.readTree(message);

            // 2) Nếu là string JSON -> parse lần 2
            if (root.isTextual()) {
                root = objectMapper.readTree(root.asText());
            }

            // 3) Nếu còn Debezium envelope thì bóc ra
            JsonNode doc = root;
            JsonNode after = root.path("payload").path("after");
            if (!after.isMissingNode() && !after.isNull()) {
                doc = after.has("fullDocument") ? after.path("fullDocument") : after;
                if (doc.isTextual()) doc = objectMapper.readTree(doc.asText());
            }

            // 4) Map ra OutboxMessage
            OutboxMessage outbox = objectMapper.treeToValue(doc, OutboxMessage.class);

            if (outbox == null || outbox.getEventType() == null) {
                log.warn("Skip: cannot parse OutboxMessage. doc={}", doc);
                ack.acknowledge();
                return;
            }

            // Inventory chỉ xử lý STOCK_CHANGED
            if (!"STOCK_CHANGED".equals(outbox.getEventType())) {
                ack.acknowledge();
                return;
            }

            // 5) dedupKey lấy từ _id
            String dedupKey = outbox.eventId();
            if (dedupKey == null || !processedEventStore.tryMarkProcessed(dedupKey)) {
                ack.acknowledge();
                return;
            }

            // 6) payloadJson là string JSON -> parse
            String payloadJson = outbox.getPayloadJson();
            if (payloadJson == null || payloadJson.isBlank()) {
                log.warn("payloadJson null/blank. outbox={}", outbox);
                ack.acknowledge();
                return;
            }

            Map<String, Object> payload = objectMapper.readValue(
                    payloadJson, new TypeReference<Map<String, Object>>() {}
            );

            @SuppressWarnings("unchecked")
            List<Map<String, Object>> changes = (List<Map<String, Object>>) payload.get("changes");

            if (changes == null || changes.isEmpty()) {
                ack.acknowledge();
                return;
            }

            for (Map<String, Object> c : changes) {
                String skuCode = (String) c.get("skuCode");
                int newStock = c.get("newStock") == null ? 0 : ((Number) c.get("newStock")).intValue();
                inventoryService.createInventory(skuCode, newStock);
            }

            ack.acknowledge();
        } catch (Exception e) {
            log.error("❌ Inventory xử lý outbox lỗi (no ack => retry)", e);
            // không ack để retry
        }
    }


    /**
     * Debezium Mongo envelope thường:
     * root.payload.after.fullDocument = outboxDoc
     * hoặc root.payload.after = outboxDoc
     * (tuỳ transforms)
     */
    private JsonNode extractOutboxDocument(JsonNode root) {
        JsonNode payload = root.path("payload");
        JsonNode after = payload.path("after");

        if (after.isMissingNode() || after.isNull()) {
            // có thể message đã unwrap sẵn -> root chính là doc
            return root;
        }

        // Mongo thường có fullDocument
        JsonNode fullDoc = after.path("fullDocument");
        if (!fullDoc.isMissingNode() && !fullDoc.isNull()) {
            return fullDoc;
        }

        // fallback
        return after;
    }

    /**
     * payload có thể được lưu:
     * - trong doc.payload (object)
     * - hoặc outbox.getPayloadJson() (string JSON)
     */
    private JsonNode extractPayloadNode(JsonNode doc, OutboxMessage outbox) throws Exception {
        // ưu tiên doc.payload nếu là object
        JsonNode p = doc.path("payload");
        if (!p.isMissingNode() && !p.isNull()) {
            // nếu payload là string -> parse
            if (p.isTextual()) return objectMapper.readTree(p.asText());
            return p; // object
        }

        // fallback từ field payloadJson trong OutboxMessage (nếu bạn lưu string)
        String payloadJson = outbox.getPayloadJson();
        if (payloadJson != null && !payloadJson.isBlank()) {
            return objectMapper.readTree(payloadJson);
        }

        return null;
    }

    private static String getString(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull() || v.isMissingNode()) return null;
        if (v.isTextual()) return v.asText();
        // Mongo _id đôi khi là object: {"$oid":"..."}
        JsonNode oid = v.get("$oid");
        if (oid != null && oid.isTextual()) return oid.asText();
        return v.toString();
    }

    private static String firstNonBlank(String... arr) {
        if (arr == null) return null;
        for (String s : arr) {
            if (s != null && !s.isBlank()) return s;
        }
        return null;
    }
}

