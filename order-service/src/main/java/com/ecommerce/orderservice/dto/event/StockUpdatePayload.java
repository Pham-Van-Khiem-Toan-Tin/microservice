package com.ecommerce.orderservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockUpdatePayload {
    private String orderId;
    private String orderNo;
    private String action; // RESTORE_STOCK hoặc CONFIRM_DEDUCTION
    private List<StockItem> items;

    @Data
    @AllArgsConstructor
    public static class StockItem {
        private String skuCode;
        private int quantity;
    }
}
