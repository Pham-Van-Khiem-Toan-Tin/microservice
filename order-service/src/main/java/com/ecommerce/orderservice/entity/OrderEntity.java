package com.ecommerce.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "t_orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    // Mã đơn hàng hiển thị cho khách (VD: ORD-2026-10293)
    // Nên dùng UUID hoặc sinh code riêng, không lộ ID tự tăng
    @Column(name = "order_number", unique = true, nullable = false)
    private String orderNumber;

    // ID người dùng (từ Auth Service)
    @Column(name = "user_id", nullable = false)
    private String userId;

    // --- CÁC CỘT VỀ TIỀN (QUAN TRỌNG) ---
    // Tổng tiền hàng chưa tính ship/giảm giá
    @Builder.Default
    @Column(name = "sub_total_amount")
    private BigDecimal subTotalAmount = BigDecimal.ZERO;
    @Builder.Default
    @Column(name = "shipping_fee")
    private BigDecimal shippingFee = BigDecimal.ZERO;
    @Builder.Default
    @Column(name = "discount_amount")
    private BigDecimal discountAmount = BigDecimal.ZERO;

    // Số tiền khách phải trả cuối cùng (= sub + ship - discount)
    @Builder.Default
    @Column(name = "final_amount", nullable = false)
    private BigDecimal finalAmount = BigDecimal.ZERO;

    // --- TRẠNG THÁI ---
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private OrderStatus status;
    @Column(name = "reservation_id")
    private String reservationId;
    @Column(name = "pay_provider")
    private String payProvider;
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status")
    private PaymentStatus paymentStatus; // UNPAID, PAID, REFUNDED
    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method")
    private PaymentMethod paymentMethod; // COD, VNPAY, BANK_TRANSFER
    @Column(name = "cancel_reason")
    private String cancelReason;
    @Column(name = "note")
    private String note;
    @Column(name = "payment_id")
    private String paymentId;
    @Column(name = "payment_url", length = 2048)
    private String paymentUrl;
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<OrderItemEntity> orderItems;

    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private OrderAddressEntity shippingAddress;
    @Column(name = "client_ip")
    private String clientIp;
    @Column(name = "paid_at")
    private LocalDateTime paidAt;
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
        if (this.status == null) this.status = OrderStatus.CREATED;
        if (this.paymentStatus == null) this.paymentStatus = PaymentStatus.INIT;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
