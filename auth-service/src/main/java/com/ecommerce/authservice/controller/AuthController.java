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

    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
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
