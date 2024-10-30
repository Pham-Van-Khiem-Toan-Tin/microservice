package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.BillingDTO;
import com.ecommerce.userservice.dto.ProfileDTO;
import com.ecommerce.userservice.service.impl.UserServiceImpl;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;


@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserServiceImpl userService;
    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    public ResponseEntity<ProfileDTO> getUserInfo(@AuthenticationPrincipal Jwt principal, @RequestHeader("Authorization") String token) {
        String userId = principal.getClaim("sub");  // "sub" là userId trong JWT
        ProfileDTO profile = userService.getUser(userId);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, token);
        String url = "http://localhost:8084/payment/profile";
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        BillingDTO billing = restTemplate.exchange(url, HttpMethod.GET, entity, BillingDTO.class).getBody();
        profile.setBilling(billing);
        return ResponseEntity.ok(profile);
    }
}

