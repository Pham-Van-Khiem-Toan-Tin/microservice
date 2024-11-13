package com.ecommerce.userservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "session")
public class SessionEntity {
    @Id
    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;
    @Column(name = "login_ip", nullable = false)
    private String ipAddress;
    @Column(name = "last_active_at", nullable = false)
    private LocalDateTime lastActiveAt;
    @Column(name = "offline_session", nullable = false)
    private Boolean offlineSession;
    @Column(name = "session_end_at")
    private LocalDateTime endAt;
    @Column(nullable = false, name = "created_at")
    private LocalDateTime createdAt;
    @Column(nullable = false, name = "is_active")
    private Boolean isActive;
    @OneToOne(mappedBy = "session", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private TokenEntity token;
}
