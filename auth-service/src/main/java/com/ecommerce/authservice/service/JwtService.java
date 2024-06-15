package com.ecommerce.authservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

@Service
public class JwtService {
    @Value("${jwt.privateKey}")
    private String encryptKey;

    private SecretKey getSecretKey() {
        byte[] keyBytes = Decoders.BASE64.decode(this.encryptKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken() {
        return Jwts.builder()
                .claim("name", "Micah Silverman")
                .claim("scope", "admins")
                .subject("Khiem")
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + 1000*60*30))
                .signWith(SignatureAlgorithm.HS256, getSecretKey())
                .compact();
    }

    public boolean validateToken(final String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSecretKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public Claims claimToken(final String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
