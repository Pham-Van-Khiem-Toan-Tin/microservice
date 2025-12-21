package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UuidGenerator;
import org.hibernate.type.SqlTypes;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.time.Instant;
import java.util.*;

@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements Serializable {
//    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue
    @UuidGenerator
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;
    @Column(nullable = false, name = "first_name")
    private String firstName;
    @Column(nullable = false, name = "last_name")
    private String lastName;
    @Column(nullable = false)
    private String email;
    @Column(nullable = false, name = "verify_email")
    private Boolean verifyEmail;
    @Column(name = "otp_email")
    private String otpEmail;
    @Column(name = "otp_email_exprie")
    private Instant otpEmailExpiration;
    @Column(nullable = false, name = "password")
    private String password;
    @Column(name = "public_avatar_id")
    private String publicAvatarId;
    @Column(name = "secure_avatar_url")
    private String secureAvatarUrl;
    @Column(name = "avatar_url")
    private String avatarUrl;
//    @JsonIgnore
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "users_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<RoleEntity> roles = new HashSet<>();
    @Column(nullable = false)
    private int status;
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;
    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;
//    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;
//    @JsonIgnore
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<UserEntity> updatedUsers = new ArrayList<>();
//    @JsonIgnore
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
