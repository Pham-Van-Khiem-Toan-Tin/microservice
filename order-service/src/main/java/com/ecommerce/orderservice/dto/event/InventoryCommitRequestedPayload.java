package com.ecommerce.orderservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryCommitRequestedPayload {
    private String orderId;
    private String reservationId;
}
