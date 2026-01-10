package com.ecommerce.orderservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Embeddable
public class OrderProductStatsDailyId implements Serializable {

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "product_id", nullable = false, length = 64)
    private String productId;
}
