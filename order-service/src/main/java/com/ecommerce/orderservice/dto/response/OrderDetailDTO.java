package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentMethod;
import com.ecommerce.orderservice.entity.PaymentStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Data
@Builder
public class OrderDetailDTO {
    private UUID id;
    private String orderNumber;
    private String userId;
    private BigDecimal subTotalAmount;
    private BigDecimal discountAmount;
    private BigDecimal finalAmount;
    private OrderStatus status;
    private PaymentStatus paymentStatus;
    private PaymentMethod paymentMethod;
    private String note;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private OrderAddressDTO orderAddress;
    private List<OrderItemDTO> orderItems;
}
