package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class UserEntity extends BaseEntity {
    @Id
    @Column(nullable = false, name = "email")
    private String email;
    @Column(nullable = false)
    private String firstName;
    @Column(nullable = false)
    private String lastName;
    @Column
    private String avatar;
    @Column
    private String phoneNumber;
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
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<UserRole> roles;

}
