package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.entity.SessionEntity;

public interface SessionService {
    String createSession(String ipAddress, String userId);
    SessionEntity findById(String sessionId);
}