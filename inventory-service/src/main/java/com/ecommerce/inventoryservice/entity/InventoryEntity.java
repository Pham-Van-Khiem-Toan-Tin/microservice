package com.ecommerce.inventoryservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(
        name="inventories",
        indexes=@Index(name = "idx_sku_code", columnList = "sku_code", unique = true),
        uniqueConstraints = @UniqueConstraint(name="uk_inventory_sku", columnNames="sku_code")
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
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
    private int totalStock = 0;

    // Số lượng đang được giữ cho đơn hàng chờ thanh toán (Reserved)
    // Available to Sell = totalStock - reservedStock
    @Column(name = "reserved_stock", nullable = false)
    private int reservedStock = 0;

    @Column(name = "min_stock_level")
    private int minStockLevel;
}
