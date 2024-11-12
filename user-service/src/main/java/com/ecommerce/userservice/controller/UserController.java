package com.ecommerce.userservice.controller;

import com.ecommerce.userservice.dto.BillingDTO;
import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.service.impl.UserServiceImpl;
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



@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @GetMapping("/profile")
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt, @RequestHeader("Authorization") String token) throws Exception {

        String userId = jwt.getClaim("sub");  // "sub" là userId trong JWT
        UserDTO user = userService.getProfile(token,userId);
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.AUTHORIZATION, token);
        String url = "http://localhost:8084/payment/profile";
        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
        BillingDTO billing = restTemplate.exchange(url, HttpMethod.GET, entity, BillingDTO.class).getBody();
        user.setBilling(billing);
        return ResponseEntity.ok(user);
    }
}

