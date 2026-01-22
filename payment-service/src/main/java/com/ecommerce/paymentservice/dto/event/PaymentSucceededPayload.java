package com.ecommerce.paymentservice.dto.event;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import lombok.*;

import java.time.LocalDateTime;

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
    private LocalDateTime paidAt;
}
