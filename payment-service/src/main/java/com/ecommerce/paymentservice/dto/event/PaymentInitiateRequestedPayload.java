package com.ecommerce.paymentservice.dto.event;

import com.ecommerce.paymentservice.enums.PaymentMethod;
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
