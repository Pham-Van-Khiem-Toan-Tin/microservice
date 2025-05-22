package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "`client`")
@NoArgsConstructor
@Data
@Builder
@AllArgsConstructor
public class ClientEntity {
    @Id
    private String id;
    private String clientId;
    private Instant clientIdIssuedAt;
    private String clientSecret;
    private Instant clientSecretExpiresAt;
    private String clientName;
    @Column(length = 1000)
    private String clientAuthenticationMethods;
    @Column(length = 1000)
    private String authorizationGrantTypes;
    @Column(length = 1000)
    private String redirectUris;
    @Column(length = 1000)
    private String postLogoutRedirectUris;
    @Column(length = 1000)
    private String scopes;
    @Column(length = 2000)
    private String clientSettings;
    @Column(length = 2000)
    private String tokenSettings;
    @OneToMany(mappedBy = "client")
    private List<RoleEntity> roles;
    @OneToMany(mappedBy = "client")
    private List<FunctionEntity> functions;
    @OneToMany(mappedBy = "client")
    private List<SubFunctionEntity> subFunctions;
    @OneToMany(mappedBy = "client")
    private Set<UserRole> userRoles;
}
