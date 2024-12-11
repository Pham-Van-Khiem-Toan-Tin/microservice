package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "auth_method")
public class ClientAuthenticationMethodEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String description;
    @ManyToMany(mappedBy = "clientAuthenticationMethods")
    private Set<ClientEntity> clients;

}
