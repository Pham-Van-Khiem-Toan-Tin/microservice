package com.ecommerce.orderservice.dto.response.order;

import com.ecommerce.orderservice.entity.OrderStatus;
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
    private PaymentMethod paymentMethod;
    private OrderStatus orderStatus;
    private String paymentUrl;
}
