package com.ecommerce.orderservice.dto.response.order;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OrderResponse {
    private String orderNumber;
    private String paymentUrl;
    private PaymentMethod paymentMethod;
}
