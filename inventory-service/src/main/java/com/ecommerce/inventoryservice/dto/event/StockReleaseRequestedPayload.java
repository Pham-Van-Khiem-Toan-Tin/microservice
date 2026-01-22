package com.ecommerce.inventoryservice.dto.event;
import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReleaseRequestedPayload {
    private String orderId;
    private String orderNumber;
    private List<Item> items;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Item {
        private String skuCode;
        private Integer qty;
    }
}
