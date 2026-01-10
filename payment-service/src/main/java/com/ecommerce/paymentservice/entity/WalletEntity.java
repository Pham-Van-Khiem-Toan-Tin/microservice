package com.ecommerce.paymentservice.entity;

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

    // Cột này dùng để chống xung đột (concurrency)
    @Version
    private Long version;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
