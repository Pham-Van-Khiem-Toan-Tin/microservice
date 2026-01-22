package com.ecommerce.orderservice.dto.event;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletRefundRequestedPayload {
    private String orderId;        // UUID string
    private String orderNumber;
    private String userId;         // cần để hoàn đúng ví
    private long amount;
    private String reason;         // LATE_PAYMENT | ORDER_CANCELLED
    private String providerRef;    // mã giao dịch (vnp/sepay/wallet)
}
