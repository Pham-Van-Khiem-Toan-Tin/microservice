package com.ecommerce.orderservice.dto.event;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentSucceededPayload {
    private String orderId;
    private String paymentId;
    private PaymentMethod method;       // WALLET | VNPAY | BANK
    private String providerRef;  // mã giao dịch
}
