package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Data
@Table(name="inventory_histories", indexes = {
        @Index(name="idx_inv_his_sku", columnList="sku_code"),
        @Index(name="idx_inv_his_ref_type", columnList="reference_id,type")
})
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
    private Instant createdAt;

    @PrePersist
    public void prePersist() {
        this.createdAt = Instant.now();
    }
}
