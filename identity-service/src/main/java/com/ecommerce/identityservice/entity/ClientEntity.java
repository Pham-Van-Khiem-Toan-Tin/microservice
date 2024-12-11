package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Set;

@Entity
@Table(name = "oauth2_clients")
@NoArgsConstructor
@Data
public class ClientEntity {
    @Id
    private String id;
    @Column(name = "client_id", nullable = false, unique = true)
    private String clientId;
    @Column(name = "client_name", nullable = false)
    private String clientName;
    @Column(name = "client_secret", nullable = false)
    private String clientSecret;
    @Column(name = "client_secret_expires_at")
    private Instant clientSecretExpiresAt;
    @Column(name = "redirect_uris")
    @Convert(converter = StringToSetConverter.class)
    private Set<String> redirectUris;
    @Column(name = "post_logout_redirect_uris")
    @Convert(converter = StringToSetConverter.class)
    private Set<String> postLogoutRedirectUris;
    @Column(name = "scopes")
    @Convert(converter = StringToSetConverter.class)
    private Set<String> scopes;
    @ManyToMany
    @JoinTable(
            name = "client_auth_method",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "auth_method_id"))
    private Set<ClientAuthenticationMethodEntity> clientAuthenticationMethods;
    @ManyToMany
    @JoinTable(
            name = "client_auth_grant_type",
            joinColumns = @JoinColumn(name = "client_id"),
            inverseJoinColumns = @JoinColumn(name = "aut_grant_type_id"))
    private Set<AuthorizationGrantTypeEntity> authorizationGrantTypes;

}
