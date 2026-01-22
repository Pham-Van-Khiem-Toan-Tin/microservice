package com.ecommerce.orderservice.dto.event;

import lombok.*;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReserveFailedPayload {
    private String orderId;   // UUID string
    private String reason;    // OUT_OF_STOCK | SKU_NOT_FOUND | ...

    // optional nhưng rất nên có để debug
    private List<FailedItem> failedItems;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FailedItem {
        private String skuCode;
        private Integer requested;
        private Long available;
    }
}
