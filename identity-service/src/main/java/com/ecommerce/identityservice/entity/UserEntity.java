package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Data
@Entity
@Table(name = "users")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity implements UserDetails {
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
    @Column(name = "public_avatar_id")
    private String publicAvatarId;
    @Column(name = "secure_avatar_url")
    private String secureAvatarUrl;
    @Column(name = "avatar_url")
    private String avatarUrl;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private RoleEntity role;
    @Column(nullable = false)
    private int status;
    @Column(nullable = false, name = "created_at")
    private Instant createdAt;
    @Column(nullable = false, name = "updated_at")
    private Instant updatedAt;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private UserEntity updatedBy;
    @OneToMany(mappedBy = "updatedBy", fetch = FetchType.LAZY)
    private List<UserEntity> updatedUsers = new ArrayList<>();
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Collections.singleton(new SimpleGrantedAuthority(this.role.getId()));
    }

    @Override
    public String getUsername() {
        return this.firstName + " " + this.lastName;
    }

    @Override
    public boolean isAccountNonLocked() {

        return this.status == 1;
    }

}
