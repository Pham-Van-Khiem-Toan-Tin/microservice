package com.ecommerce.inventoryservice.dto.event;

import lombok.*;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedResult implements Serializable {
    private String orderNumber;
    private BigDecimal amount;
    private PaymentMethod method;
    private String reason;          // VNPAY_FAILED | USER_CANCELLED | ...
    private LocalDateTime failedAt;
}
