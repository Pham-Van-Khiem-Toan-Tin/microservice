package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.service.impl.UserServiceImpl;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserServiceImpl userService;
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('USER_LIST')")
    public ResponseEntity<UserRepresentation> getUserInfo(@AuthenticationPrincipal Jwt principal) {
        String userId = principal.getClaim("sub");  // "sub" là userId trong JWT
        UserRepresentation user = userService.getUser(userId);
        return ResponseEntity.ok(user);
    }
}

