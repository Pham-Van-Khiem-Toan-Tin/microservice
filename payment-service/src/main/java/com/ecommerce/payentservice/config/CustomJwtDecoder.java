package com.ecommerce.payentservice.config;

import com.ecommerce.identityservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;

@Component
public class CustomJwtDecoder implements JwtDecoder {
    @Value("${jwt.privateKey}")
    private String privateKey;
    @Override
    public Jwt decode(String token) throws JwtException {
        try {
            SecretKey secretKey = JwtUtils.getSecretKey(privateKey);
            Claims claims = JwtUtils.claimToken(token, secretKey);
            return new Jwt(
                    token,
                    claims.getIssuedAt().toInstant(),
                    claims.getExpiration().toInstant(),
                    claims,
                    claims
            );
        } catch (JwtException e) {
            throw new JwtException("Invalid token or signature verification failed", e);
        }
    }
}
