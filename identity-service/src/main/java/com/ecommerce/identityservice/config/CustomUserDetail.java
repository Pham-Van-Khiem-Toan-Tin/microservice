package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.entity.UserEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.type.TypeReference;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public class CustomUserDetail implements UserDetails, Serializable {
    private static final long serialVersionUID = 1L;
    private UUID id;            // Quan trọng nhất: Để lấy ID người dùng
    private String email;       // Dùng làm username
    private String fullName;    // Để hiển thị "Xin chào, ..."
    private String password;    // Để Spring check pass
    private int status;
    private Set<GrantedAuthority> authorities;
    public CustomUserDetail(UserEntity user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.fullName = user.getFullName(); // Lấy giá trị chuỗi, cắt đứt quan hệ logic
        this.password = user.getPassword();
        this.status = user.getStatus();

        // Map roles sang Collection ngay tại đây
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority(role.getName()))
                .collect(Collectors.toSet());
    }
    @JsonIgnore
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {

        return this.authorities;
    }
    @JsonIgnore
    @Override
    public String getPassword() {
        return this.password;
    }
    @JsonIgnore
    @Override
    public String getUsername() {
        return this.fullName;
    }
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {

        return this.status != 2;
    }
}
