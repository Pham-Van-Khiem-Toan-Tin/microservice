package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.config.KeycloakConfig;

import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.service.UserService;
import org.keycloak.representations.idm.GroupRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl implements UserService {
    @Value("${keycloak.realm}")
    private String realm;
    @Autowired
    KeycloakConfig keycloak;



    @Override
    public UserDTO getProfile(String userId) {
        UserRepresentation userRepresentation = keycloak
                .getKeycloakInstance()
                .realm(realm)
                .users()
                .get(userId)
                .toRepresentation();
        UserDTO userDTO = new UserDTO();
        userDTO.setFirstName(userRepresentation.getFirstName());
        userDTO.setLastName(userRepresentation.getLastName());
        userDTO.setAvatar(userRepresentation.getAttributes().get("avatar").get(0));
        userDTO.setPhoneNumber(userRepresentation.getAttributes().get("phoneNumber").get(0));
        userDTO.setEmail(userRepresentation.getEmail());
        List<GroupRepresentation> groupRepresentations = keycloak
                .getKeycloakInstance()
                .realm(realm)
                .users()
                .get(userId)
                .groups();
        String group = groupRepresentations.get(0).getName();
        userDTO.setRole(capitalizeFirstLetter(group));
        return userDTO;
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.substring(0,1).toUpperCase() + lowerCase.substring(1);
    }
}
