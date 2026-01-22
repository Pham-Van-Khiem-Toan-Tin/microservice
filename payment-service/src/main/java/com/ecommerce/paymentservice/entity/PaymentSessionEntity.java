package com.ecommerce.paymentservice.entity;

import com.ecommerce.paymentservice.enums.PaymentMethod;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "t_payment_sessions",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_order", columnNames = {"order_id"})
        }
)
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class PaymentSessionEntity {

    @Id
    @Column(name = "id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID id;

    @Column(name = "order_id", nullable = false, columnDefinition = "BINARY(16)")
    private UUID orderId;

    @Column(name = "order_number", length = 64, nullable = false)
    private String orderNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "method", length = 20, nullable = false)
    private PaymentMethod method; // VNPAY/BANK/WALLET

    @Column(name = "amount", nullable = false)
    private Long amount;

    @Column(name = "payment_url", columnDefinition = "TEXT")
    private String paymentUrl;

    @Column(name = "qr_content", columnDefinition = "TEXT")
    private String qrContent;

    @Column(name = "status", length = 20, nullable = false)
    private String status; // INIT, PENDING, SUCCEEDED, FAILED

    @Column(name = "provider_ref", length = 128)
    private String providerRef;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
