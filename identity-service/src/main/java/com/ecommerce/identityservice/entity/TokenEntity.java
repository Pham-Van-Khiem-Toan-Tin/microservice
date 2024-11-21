package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "token")
public class TokenEntity {
    @Id
    private String id;
    @Column(name = "refresh_token", nullable = false)
    private String refreshToken;
    @OneToMany(mappedBy = "token")
    private List<SessionEntity> sessions;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
