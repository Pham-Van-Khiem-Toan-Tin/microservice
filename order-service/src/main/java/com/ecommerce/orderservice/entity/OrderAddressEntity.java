package com.ecommerce.orderservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

@Entity
@Table(name = "t_order_address")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderAddressEntity {

    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private OrderEntity order;

    @Column(name = "contact_name", nullable = false)
    private String contactName;

    @Column(name = "phone", nullable = false)
    private String phone;

    @Column(name = "address_detail")
    private String addressDetail; // Số nhà, đường...

    @Column(name = "city")
    private String city;     // Tỉnh/Thành

    @Column(name = "district")
    private String district; // Quận/Huyện

    @Column(name = "ward")
    private String ward;     // Phường/Xã
}
