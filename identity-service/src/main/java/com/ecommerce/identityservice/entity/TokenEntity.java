package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "token")
public class TokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;
    @OneToOne
    @JoinColumn(name = "session_id", referencedColumnName = "id", unique = true)
    private SessionEntity session;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
