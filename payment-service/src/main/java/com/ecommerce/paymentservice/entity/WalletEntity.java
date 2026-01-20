package com.ecommerce.paymentservice.entity;

import com.ecommerce.paymentservice.enums.WalletStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "t_wallets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WalletEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    // Luôn dùng BigDecimal cho tiền tệ, mặc định là 0
    @Column(name = "balance", nullable = false)
    private BigDecimal balance;
    @Enumerated(EnumType.STRING)
    private WalletStatus status;
    private String currency;
    // Cột này dùng để chống xung đột (concurrency)
    @Version
    private Long version;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (balance == null) balance = BigDecimal.ZERO;
        if (currency == null) currency = "VND";
        if (status == null) status = WalletStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
