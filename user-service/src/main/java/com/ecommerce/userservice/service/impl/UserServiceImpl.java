package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.service.UserService;
import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    @Value("${keycloak.auth-server-url}")
    private String serverUrl;
    @Value("${keycloak.realm}")
    private String realm;
    @Value("${keycloak.resource}")
    private String clientId;
    private Keycloak keycloak;

    public UserServiceImpl() {
        this.keycloak = KeycloakBuilder.builder()
                .serverUrl("http://localhost:8086/admin/realms")
                .realm("ecobazar")
                .grantType(OAuth2Constants.PASSWORD)
                .clientId("identity-client")
                .username("admin@gmail.com")
                .password("admin")
                .build();
    }

    public UserRepresentation getUser(String userId) {
        return keycloak.realm("ecobazar").users().get(userId).toRepresentation();
    }
}
