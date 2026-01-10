package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "inventories", indexes = {
        @Index(name = "idx_sku_code", columnList = "sku_code", unique = true)
})
@Data
public class InventoryEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @Column(name = "sku_code", nullable = false, unique = true)
    private String skuCode;

    // Tổng số lượng thực tế trong kho (Physical Stock)
    @Column(name = "total_stock", nullable = false)
    private Integer totalStock;

    // Số lượng đang được giữ cho đơn hàng chờ thanh toán (Reserved)
    // Available to Sell = totalStock - reservedStock
    @Column(name = "reserved_stock", nullable = false)
    private Integer reservedStock;

    @Column(name = "min_stock_level")
    private Integer minStockLevel;
}
