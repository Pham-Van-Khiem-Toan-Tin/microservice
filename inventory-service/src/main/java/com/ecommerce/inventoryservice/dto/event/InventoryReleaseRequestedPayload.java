package com.ecommerce.inventoryservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryReleaseRequestedPayload {
    private String orderId;
    private String reservationId;
    private String reason;
}
