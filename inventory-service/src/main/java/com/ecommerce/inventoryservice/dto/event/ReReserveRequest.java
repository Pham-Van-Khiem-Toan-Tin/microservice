package com.ecommerce.inventoryservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ReReserveRequest {
    private String orderId;        // ID gốc để mapping hệ thống
    private String orderNumber;  // Mã "đẹp" TZ-2026... dùng để đối soát từ Payment
    private List<ReReserveItem> items;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ReReserveItem {
        private String skuCode;
        private Integer quantity;
    }
}