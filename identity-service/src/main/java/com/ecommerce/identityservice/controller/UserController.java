package com.ecommerce.identityservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RequestMapping("/test")
@RestController
public class UserController {
    @GetMapping
    public ResponseEntity<String> test() {
        Authentication authentication = (Authentication) SecurityContextHolder.getContext().getAuthentication();
        List<GrantedAuthority> test = (List<GrantedAuthority>) authentication.getAuthorities();
        return ResponseEntity.ok("test");
    }
}
