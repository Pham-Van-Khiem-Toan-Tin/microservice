package com.ecommerce.inventoryservice.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReleasedPayload {
    private String orderId;
    private String orderNumber;
    private LocalDateTime releasedAt;
}
