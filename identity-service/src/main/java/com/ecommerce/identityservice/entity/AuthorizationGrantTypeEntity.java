package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_grant_type")
public class AuthorizationGrantTypeEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String description;
    @ManyToMany(mappedBy = "authorizationGrantTypes")
    private Set<ClientEntity> clients;
}
