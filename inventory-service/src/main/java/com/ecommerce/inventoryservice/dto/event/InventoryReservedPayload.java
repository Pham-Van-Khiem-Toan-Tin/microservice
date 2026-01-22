package com.ecommerce.inventoryservice.dto.event;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservedPayload {
    private String orderId;        // UUID string
    private String reservationId;  // UUID string
    private LocalDateTime expireAt;

    private List<Item> items;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class Item {
        private String skuCode;
        private Integer qty;
    }
}
