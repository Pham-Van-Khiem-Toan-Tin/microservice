package com.ecommerce.orderservice.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

public class AuthenticationUtils {
    public static String getUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = null;
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            userId = jwt.getSubject();
        }
        return userId;
    }
    public static String currentBearerToken() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String bearerToken = null;
        if (auth instanceof JwtAuthenticationToken jwtAuth) {
            bearerToken = "Bearer " + jwtAuth.getToken().getTokenValue();
        }
        return bearerToken;
    }
}
