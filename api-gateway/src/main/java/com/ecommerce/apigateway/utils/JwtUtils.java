package com.ecommerce.apigateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;


public class JwtUtils {

    public static SecretKey getSecretKey(String encryptKey) {
        byte[] keyBytes = Decoders.BASE64.decode(encryptKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public static String generateToken(String subject,Map<String, Object> claim, long expiration, SecretKey secretKey) {
        return Jwts.builder()
                .subject(subject)
                .claims(claim)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(expiration))
                .signWith(SignatureAlgorithm.HS256, secretKey)
                .compact();
    }

    public static boolean validateToken(final String token, SecretKey secretKey) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public static Claims claimToken(final String token, SecretKey secretKey) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
