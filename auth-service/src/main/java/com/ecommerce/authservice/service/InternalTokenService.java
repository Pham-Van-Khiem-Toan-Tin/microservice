package com.ecommerce.authservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
public class InternalTokenService {
    private final JwtEncoder encoder;

    @Value("${bff.internal-jwt.issuer:http://bff}")
    private String issuer;

    @Value("${bff.internal-jwt.ttl-seconds:300}")
    private long ttlSeconds;

    public InternalTokenService(JwtEncoder encoder) {
        this.encoder = encoder;
    }

    public String mint(Authentication auth) {
        Instant now = Instant.now();

        List<String> authorities = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .distinct()
                .toList();

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer(issuer)
                .issuedAt(now)
                .expiresAt(now.plusSeconds(ttlSeconds))
                .subject(auth.getName()) // hoặc userId
                .claim("authorities", authorities)
                .build();

        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256)
                .keyId("bff-key-1")
                .build();

        return encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}

