package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
import com.ecommerce.identityservice.repository.TokenRepository;
import com.ecommerce.identityservice.service.TokenService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class TokenServiceImpl implements TokenService {
    @Autowired
    EntityManager entityManager;
    @Autowired
    TokenRepository tokenRepository;
    @Override
    public TokenEntity createToken(String refreshToken, String sessionId) {
        TokenEntity token = new TokenEntity();
        token.setRefreshToken(refreshToken);
        token.setCreatedAt(LocalDateTime.now());
        token.setSession(entityManager.getReference(SessionEntity.class, sessionId));
        return tokenRepository.save(token);
    }
}
