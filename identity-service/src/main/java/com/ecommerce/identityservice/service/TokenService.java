package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.TokenEntity;

public interface TokenService {
    String createToken(String refreshToken, String sessionId);
}
