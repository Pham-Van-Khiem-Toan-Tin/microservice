package com.ecommerce.orderservice.dto.event;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentInitiatedPayload {
    private String orderId;
    private String paymentId;     // UUID string (payment session id)
    private PaymentMethod method;        // VNPAY | BANK
    private String paymentUrl;    // redirect link
    private String qrContent;     // optional: nếu bank QR cần EMV string
    private String expiredAt;     // optional string ISO
}
