package com.ecommerce.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "order_stats_daily")
public class OrderStatsDaily {

    @Id
    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "total_orders", nullable = false)
    private Long totalOrders;

    @Column(name = "delivered_orders", nullable = false)
    private Long deliveredOrders;

    @Column(name = "cancelled_orders", nullable = false)
    private Long cancelledOrders;

    @Column(name = "revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal revenue;

    @Column(name = "gross_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal grossAmount;

    @Column(name = "discount_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal discountAmount;

    @Column(name = "items_sold", nullable = false)
    private Long itemsSold;

    @Column(name = "cod_orders", nullable = false)
    private Long codOrders;

    @Column(name = "vnpay_orders", nullable = false)
    private Long vnpayOrders;

    @Column(name = "bank_transfer_orders", nullable = false)
    private Long bankTransferOrders;

    /**
     * DB tự set DEFAULT CURRENT_TIMESTAMP và ON UPDATE CURRENT_TIMESTAMP
     * -> để read-only ở Java, tránh JPA update ngược lại
     */
    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
