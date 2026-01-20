package com.ecommerce.orderservice.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OutboxMessage {
    @JsonProperty("_id")
    private Object id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;     // "PRODUCT_UPSERT"
    private String payloadJson;

    public String eventId() {
        return id == null ? null : id.toString();
    }
}
