package com.ecommerce.orderservice.dto.event;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiateRequestedPayload {
    private String orderId;
    private String orderNumber;
    private String userId;
    private String clientIp;
    private long amount;
    private PaymentMethod method; // VNPAY/BANK/WALLET
}
