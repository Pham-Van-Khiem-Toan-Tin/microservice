package com.ecommerce.catalogservice.entity;


import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.Instant;
import java.util.Map;

@Document(collection = "outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {

    /**
     * Mongo _id → dùng luôn làm eventId để consumer dedupe
     */
    @Id
    private ObjectId id;

    /**
     * Loại aggregate (vd: Product, Order, User...)
     */
    @Field("aggregateType")
    @Indexed
    private String aggregateType;

    /**
     * ID của aggregate (vd: productId, orderId)
     * → dùng làm Kafka key khi cần
     */
    @Field("aggregateId")
    @Indexed
    private String aggregateId;

    /**
     * Loại event nghiệp vụ
     * vd: PRODUCT_UPSERT, STOCK_CHANGED, PRICE_CHANGED
     */
    @Field("eventType")
    @Indexed
    private OutboxEventType eventType;

    /**
     * Idempotency key từ request (optional nhưng rất khuyên dùng)
     * → chống retry tạo trùng
     */
    @Field("idempotencyKey")
    @Indexed(unique = true)
    private String idempotencyKey;

    /**
     * Payload event (JSON string để ổn định schema)
     */

    private String payloadJson;

    private Instant occurredAt;
    private Instant createdAt;
}

