package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.config.KeycloakConfig;
import com.ecommerce.userservice.service.UserService;
import jakarta.annotation.PostConstruct;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {
    @Value("${keycloak.realm}")
    private String realm;
    @Autowired
    KeycloakConfig keycloak;


    public UserRepresentation getUser(String userId) {
        return keycloak.getKeycloakInstance().realm(realm).users().get(userId).toRepresentation();
    }
}
