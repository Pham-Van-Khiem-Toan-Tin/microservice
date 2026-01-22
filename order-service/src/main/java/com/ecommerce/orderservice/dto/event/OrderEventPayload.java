package com.ecommerce.orderservice.dto.event;

import com.ecommerce.orderservice.entity.PaymentMethod;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class OrderEventPayload {
    private String orderId;
    private List<OrderItemPayload> items;
    private String userId;
    private PaymentMethod paymentMethod;
    private String orderNumber;
    private BigDecimal finalAmount;
    private LocalDateTime expiresAt;
    @Data
    @AllArgsConstructor
    public static class OrderItemPayload {
        private String skuCode;
        private Integer quantity;
    }
}
