package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Map;

@Slf4j
@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.privateKey}")
    private String privateKey;
    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SecretKey secretKey = JwtUtils.getSecretKey(privateKey);
            Claims claims = JwtUtils.claimToken(token, secretKey);
            Instant issuedAt = Instant.ofEpochMilli(claims.getIssuedAt().getTime());
            Instant expiresAt = Instant.ofEpochMilli(claims.getExpiration().getTime());
            return new Jwt(token, issuedAt, expiresAt, claims, claims);
        } catch (Exception e) {
            throw new JwtException("Không thể giải mã token: " + e.getMessage());
        }
    }
}
