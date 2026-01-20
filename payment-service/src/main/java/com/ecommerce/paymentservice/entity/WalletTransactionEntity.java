package com.ecommerce.paymentservice.entity;


import com.ecommerce.paymentservice.enums.TransactionStatus;
import com.ecommerce.paymentservice.enums.TransactionType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "t_wallet_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletTransactionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wallet_id", nullable = false)
    private WalletEntity wallet;

    // Số tiền giao dịch (Dương là nạp/hoàn, Âm là rút/mua)
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "balance_before")
    private BigDecimal balanceBefore;
    @Column(name = "balance_after")
    private BigDecimal balanceAfter;
    // Loại giao dịch
    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private TransactionType type;
    // Enum: DEPOSIT (Nạp), WITHDRAW (Rút), PAYMENT (Thanh toán đơn), REFUND (Hoàn tiền)

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private TransactionStatus status;
    // Enum: PENDING, SUCCESS, FAILED

    // Mã tham chiếu (VD: Mã đơn hàng ORD-123, hoặc Mã giao dịch VNPAY)
    @Column(name = "reference_code")
    private String referenceCode;

    @Column(name = "description")
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
