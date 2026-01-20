package com.ecommerce.paymentservice.entity;

import com.ecommerce.paymentservice.enums.PaymentType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "payment_transactions") // Tên bảng khác bảng ví nhé
@Getter @Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentTransactionEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    // Các trường khớp với dữ liệu SePay gửi sang
    @Column(name = "user_id")
    private String userId;           // Parse từ nội dung (VD: "101")

    @Column(name = "amount")
    private BigDecimal amount;       // Số tiền nạp

    @Column(name = "gateway")
    private String gateway;          // VD: Vietcombank, MB

    @Column(name = "external_trans_id", unique = true)
    private String externalTransId;  // ID của SePay (quan trọng để chống trùng)
    @Column(name = "reference_id")
    private String referenceId;
    @Column(name = "type")
    private PaymentType type;
    @Column(name = "content")
    private String originalContent;  // Lưu lại nội dung gốc (VD: "TZNAP 101")

    @Column(name = "status")
    private String status;           // SUCCESS

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}