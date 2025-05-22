package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_client_roles")
public class UserRole {
    @EmbeddedId
    private UserRoleId id = new UserRoleId();
    @ManyToOne
    @MapsId("userId")
    @JoinColumn(name = "user_id")
    @JsonIgnore
    private UserEntity user;
    @ManyToOne
    @MapsId("clientId")
    @JoinColumn(name = "client_id")
    @JsonIgnore
    private ClientEntity client;
    @ManyToOne
    @JsonIgnore
    @JoinColumn(name = "role_id")
    private RoleEntity role;
}
