package com.ecommerce.orderservice.dto.response.order;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderCustomerResponse {
    private String orderId;
    private String orderNumber;
    private BigDecimal finalAmount;
    private String status;
    private String paymentStatus;
    private String paymentMethod;
    private LocalDateTime createdAt;
    private List<OrderItemResponse> items;

    // Đếm tổng số lượng sản phẩm trong đơn để hiển thị text "và X sản phẩm khác"
    private int totalItemsCount;
}
