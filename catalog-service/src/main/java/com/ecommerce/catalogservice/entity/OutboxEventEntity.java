package com.ecommerce.catalogservice.entity;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.Map;

@Document("outbox_events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxEventEntity {
    @Id
    private String id;

    private OutboxEventType type;

    @Indexed
    private String aggregateId; // productId

    @Indexed
    private OutboxStatus status;

    private int retryCount;
    private Instant nextRetryAt;

    private Map<String, Object> payload;

    private Instant createdAt;
    private Instant updatedAt;
}
