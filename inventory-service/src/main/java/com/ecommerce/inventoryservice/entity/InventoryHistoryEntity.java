package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "inventory_histories")
@Data
public class InventoryHistoryEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @Column(name = "sku_code")
    private String skuCode;

    @Column(name = "qty_change")
    private Integer quantityChange; // +10 hoặc -5

    @Column(name = "stock_after")
    private Integer stockAfter;

    // Enum: IMPORT, RESERVED, SOLD, CANCELLED, RETURNED
    @Enumerated(EnumType.STRING)
    private InventoryType type;

    private String referenceId; // Order ID hoặc Import Ticket ID

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
