package com.ecommerce.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrderStatsOverviewDTO {
    private long totalOrders;
    private long deliveredOrders;
    private long cancelledOrders;

    private BigDecimal revenue;
    private BigDecimal grossAmount;
    private BigDecimal discountAmount;

    private long itemsSold;

    private long codOrders;
    private long vnpayOrders;
    private long bankTransferOrders;
}