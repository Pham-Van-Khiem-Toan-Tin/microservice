package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.TokenForm;
import com.ecommerce.authservice.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

@RestController
@RequestMapping("/api/token")
public class TokenController {
    @Autowired
    JwtService jwtService;
    @GetMapping
    public String test() {
        String token = jwtService.generateToken();
        System.out.println(token);
        return token;
    }
    @PostMapping
    public String test2(@RequestBody TokenForm tokenForm) {
        Claims claims = jwtService.claimToken(tokenForm.getToken());
        return claims.toString();
    }
}
