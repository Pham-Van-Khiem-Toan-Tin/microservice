package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.model.UserEntity;
import com.ecommerce.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class UserApiController {
    @Autowired
    UserService userService;
    @PostMapping("/gentoken")
    public ResponseEntity<String> gentoken() {
        return ResponseEntity.ok("login success");
    }
    @PostMapping("/login")
    public ResponseEntity<String> login() {
        return ResponseEntity.ok("login success");
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@ModelAttribute UserEntity user) {
        System.out.println("chay vao day");
        userService.register(user);
        return ResponseEntity.ok("register successfully");
    }
}

