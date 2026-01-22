package com.ecommerce.paymentservice.dto.event;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentFailedPayload {
    private String orderId;
    private String paymentId;
    private PaymentMethod method;
    private String reason;
}
