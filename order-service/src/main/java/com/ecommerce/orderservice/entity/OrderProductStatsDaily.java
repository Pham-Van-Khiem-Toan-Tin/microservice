package com.ecommerce.orderservice.entity;

import jakarta.persistence.*;
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
@Table(
        name = "order_product_stats_daily",
        indexes = {
                @Index(name = "idx_qty", columnList = "stat_date,quantity")
        }
)
public class OrderProductStatsDaily {

    @EmbeddedId
    private OrderProductStatsDailyId id;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "quantity", nullable = false)
    private Long quantity;

    @Column(name = "revenue", nullable = false, precision = 18, scale = 2)
    private BigDecimal revenue;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    // tiện dùng hơn khi mapping DTO
    @Transient
    public LocalDate getStatDate() {
        return id != null ? id.getStatDate() : null;
    }

    @Transient
    public String getProductId() {
        return id != null ? id.getProductId() : null;
    }
}
