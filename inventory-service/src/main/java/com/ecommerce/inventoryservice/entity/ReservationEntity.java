package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "t_inventory_reservations",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_id", columnNames = "order_id"),
        indexes = {
                @Index(name = "idx_reservation_expire", columnList = "status,expire_at")
        })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationEntity {

    @Id
    @Column(
            name = "id",
            nullable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID id;   // reservationId

    @Column(
            name = "order_id",
            nullable = false,
            columnDefinition = "BINARY(16)"
    )
    private UUID orderId;   // orderId (business key, UNIQUE)

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private ReservationStatus status;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "reservation",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<ReservationItemEntity> items = new ArrayList<>();

    public void addItem(ReservationItemEntity item) {
        items.add(item);
        item.setReservation(this);
    }
}

