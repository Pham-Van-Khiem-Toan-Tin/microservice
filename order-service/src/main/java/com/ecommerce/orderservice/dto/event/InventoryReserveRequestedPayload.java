package com.ecommerce.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveRequestedPayload {
    private boolean refund;
    private String orderId;
    private String orderNumber;
    private LocalDateTime paidAt;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String skuCode;
        private Integer qty;
    }
}
