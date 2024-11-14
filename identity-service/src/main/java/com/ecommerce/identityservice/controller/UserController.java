package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.BillingDTO;
import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;



@RestController
@RequestMapping("/identity/user")
public class UserController {
    @Autowired
    UserServiceImpl userService;
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("test");
    }
//    @GetMapping("/profile")
//    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
//    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt, @RequestHeader("Authorization") String token) throws Exception {
//
//        String userId = jwt.getClaim("sub");  // "sub" là userId trong JWT
//        UserDTO user = userService.getProfile(token,userId);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set(HttpHeaders.AUTHORIZATION, token);
//        String url = "http://localhost:8084/payment/profile";
//        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
//        BillingDTO billing = restTemplate.exchange(url, HttpMethod.GET, entity, BillingDTO.class).getBody();
//        user.setBilling(billing);
//        return ResponseEntity.ok(user);
//    }
}

