package com.ecommerce.inventoryservice.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "inventory_items",
        indexes = {
                @Index(name = "idx_item_sku", columnList = "sku_code"),
                @Index(name = "idx_item_serial", columnList = "serial_number", unique = true)
        }
)
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InventoryItemEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    private UUID id;

    @Column(name = "sku_code", nullable = false)
    private String skuCode;

    @Column(name = "serial_number", nullable = false, unique = true)
    private String serialNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryItemStatus status;

    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "order_number")
    private String orderNumber;
    @Column(name = "reserved_at")
    private LocalDateTime reservedAt;
    @Column(name = "sold_at")
    private LocalDateTime soldAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inventory_id", nullable = false)
    private InventoryEntity inventory;
}
