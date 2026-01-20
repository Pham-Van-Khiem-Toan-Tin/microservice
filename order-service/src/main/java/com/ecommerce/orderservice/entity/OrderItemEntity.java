package com.ecommerce.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "t_order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    // Liên kết với Order cha
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    // --- THÔNG TIN SẢN PHẨM (SNAPSHOT) ---
    // Lưu cứng lại, không join bảng Product

    @Column(name = "sku_code", nullable = false)
    private String skuCode; // Để trừ kho bên Inventory
    @Column(name = "sku_id", nullable = false)
    private String skuId;
    @Column(name = "product_id", nullable = false)
    private String productId; // Để review hoặc click xem lại

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "variant_name")
    private String variantName; // VD: "Màu Đen - 256GB"

    @Column(name = "product_thumbnail")
    private String productThumbnail;
    @Builder.Default
    @Column(name = "is_reviewed", nullable = false)
    private boolean reviewed = false;
    // --- GIÁ VÀ SỐ LƯỢNG ---
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false)
    private BigDecimal unitPrice; // Giá tại thời điểm mua

    @Column(name = "sub_total", nullable = false)
    private BigDecimal subTotal; // = quantity * unitPrice
}
