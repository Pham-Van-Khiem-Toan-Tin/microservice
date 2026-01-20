package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserAddressEntity {
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY) // Lazy để khi lấy Address không tự load User (nặng)
    @JoinColumn(name = "user_id", nullable = false) // Tên cột khóa ngoại trong DB
    @JsonIgnore
    private UserEntity user;

    @Column(name = "receiver_name", nullable = false)
    private String receiverName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "detail_address", nullable = false)
    private String detailAddress;

    // --- LOCATION INFO (Lưu String để khớp với bảng locations) ---
    @Column(name = "province_code", length = 20)
    private String provinceCode;

    @Column(name = "province_name")
    private String provinceName;

    @Column(name = "district_code", length = 20)
    private String districtCode;

    @Column(name = "district_name")
    private String districtName;

    @Column(name = "ward_code", length = 20)
    private String wardCode;

    @Column(name = "ward_name")
    private String wardName;

    @Column(name = "is_default")
    private Boolean isDefault = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "type")
    private AddressType type;

    // Audit fields
    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;
}
