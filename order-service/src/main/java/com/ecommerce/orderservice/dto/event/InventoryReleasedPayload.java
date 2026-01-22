package com.ecommerce.orderservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReleasedPayload {
    private String orderId;
    private String reservationId;
    private String reason; // PAYMENT_FAILED / ORDER_CANCELLED / EXPIRED
}
