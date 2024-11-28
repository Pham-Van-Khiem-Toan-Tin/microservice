package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.BillingDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.TestDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.service.UserService;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/base")
    public ApiResponse<AuthProfileDTO> base() throws CustomException {
        return new ApiResponse<>(200, userService.getAuthProfile());
    }
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public ApiResponse<ProfileDetailDTO> profile(HttpServletRequest request) throws CustomException {
        ProfileDetailDTO profileDetailDTO = userService.getProfile();
        return new ApiResponse<>(200, profileDetailDTO);
    }

    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/test")
    public String test() throws CustomException {

        return "test";
    }

}
