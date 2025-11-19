package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "users")
@Data
public class UserEntity {
    @Id
    private  String id;

    @Column(nullable = false, length = 100)
    private String username;
    @Column(nullable = false, length = 100)
    private String email;
    @Column(nullable = false)
    private int status;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id", nullable = false)
    private RoleEntity role;
}
