package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    UserService userService;
    @PostMapping("/register")
    public ResponseEntity<String> register(@RequestBody RegisterForm registerForm) {
        userService.register(registerForm);
        return ResponseEntity.ok("register successfully");
    }
    @GetMapping("/users")
    public ResponseEntity<List<UserDTO>> getAllUser() {
        return ResponseEntity.ok(userService.getAllUser());
    }
}

