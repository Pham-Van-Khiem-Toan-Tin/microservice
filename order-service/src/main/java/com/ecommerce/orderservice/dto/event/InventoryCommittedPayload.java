package com.ecommerce.orderservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCommittedPayload {
    private String orderId;        // UUID string
    private String reservationId;  // UUID string
}
