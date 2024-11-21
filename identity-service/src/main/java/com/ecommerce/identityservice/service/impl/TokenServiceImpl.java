package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
import com.ecommerce.identityservice.repository.TokenRepository;
import com.ecommerce.identityservice.service.TokenService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    TokenRepository tokenRepository;
    @Override
    public String createToken(String refreshToken, String sessionId) {
        String tokenId = UUID.randomUUID().toString();
        TokenEntity token = new TokenEntity();
        token.setId(tokenId);
        token.setRefreshToken(refreshToken);
        token.setCreatedAt(LocalDateTime.now());
        tokenRepository.save(token);
        return tokenId;
    }
}
