package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.response.AuthDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.stream.Collectors;

@RestController
public class AuthController {
    @Autowired
    PasswordEncoder passwordEncoder;
    private static final String IDP_AUTHORIZE_URL = "http://127.0.0.1:8085/oauth2/authorize";
    private static final String CLIENT_ID = "admin-client";
    private static final String REDIRECT_URI = "http://127.0.0.1:8082/auth/admin/callback";
    private String generateStateToken() {
        byte[] bytes = new byte[24]; // 24 bytes ~ 32 ký tự base64
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/me")
    public ResponseEntity<AuthDTO> authenticatedProfile(Authentication authentication) {
        OAuth2AuthenticationToken oat = (OAuth2AuthenticationToken) authentication;
        OAuth2User user = oat.getPrincipal();

        return ResponseEntity.ok(AuthDTO.builder()
                .userId(user.getAttribute("uid"))              // sub / uid
                .userName(user.getAttribute("sub"))
                .permissions(oat.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet()))// hoặc "preferred_username"
                .build());
    }

}
