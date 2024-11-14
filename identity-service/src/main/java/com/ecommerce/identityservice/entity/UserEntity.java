package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class UserEntity extends BaseEntity {
    @Id
    private String id;
    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false, name = "login_fail_count")
    private Integer loginFailCount;
    @Column(nullable = false)
    private Boolean block;
    @Column(name = "lock_time")
    private LocalDateTime lockTime;
    @Column(name = "unlock_time")
    private LocalDateTime unlockTime;
    @OneToMany(mappedBy = "user")
    private Set<SessionEntity> sessions;
    @ManyToOne
    @JoinColumn(name = "role", nullable = false)
    private RoleEntity role;

}
