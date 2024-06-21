package com.ecommerce.authservice.config;

import com.ecommerce.authservice.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Component
public class CustomLoginSuccesshandler implements AuthenticationSuccessHandler {
    @Autowired
    JwtService jwtService;
    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authentication) throws IOException, ServletException {

    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        String userId = authentication.getName();
        Map<String, String> userData = new HashMap<>();
        userData.put("id", userId);
        String accessToken = jwtService.generateToken(userData);
        Map<String, String> tokenData = new HashMap<>();
        response.addHeader(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken);
        // You can customize response here if needed
        response.setStatus(HttpStatus.OK.value());
    }
}
