package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.TokenEntity;

public interface TokenService {
    TokenEntity createToken(String refreshToken, String sessionId);
}
