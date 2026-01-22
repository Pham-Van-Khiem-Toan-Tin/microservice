package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "t_reservation_items",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_reservation_sku", columnNames = {"reservation_id", "sku_code"})
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReservationItemEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "reservation_id",
            nullable = false,
            columnDefinition = "BINARY(16)",
            foreignKey = @ForeignKey(name = "fk_reservation_item")
    )
    private ReservationEntity reservation;

    @Column(name = "sku_code", length = 64, nullable = false)
    private String skuCode;

    @Column(name = "qty", nullable = false)
    private Integer qty;
}
