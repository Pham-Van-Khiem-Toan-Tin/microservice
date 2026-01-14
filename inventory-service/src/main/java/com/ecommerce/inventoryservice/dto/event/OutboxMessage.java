package com.ecommerce.inventoryservice.dto.event;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;

@Data
public class OutboxMessage {
    @JsonProperty("_id")
    private JsonNode id;

    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payloadJson;

    @JsonIgnore
    public String eventId() {
        if (id == null || id.isNull()) return null;
        // _id dạng {"$oid":"..."}
        JsonNode oid = id.get("$oid");
        if (oid != null && oid.isTextual()) return oid.asText();
        // _id dạng string
        if (id.isTextual()) return id.asText();
        // fallback
        return id.toString();
    }
}
