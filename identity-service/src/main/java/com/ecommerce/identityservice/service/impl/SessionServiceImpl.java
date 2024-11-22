package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.repository.SessionRepository;
import com.ecommerce.identityservice.service.SessionService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class SessionServiceImpl implements SessionService {
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    EntityManager entityManager;
    @Override
    public String createSession(String ipAddress, String userId) {
        String sessionId = UUID.randomUUID().toString();
        LocalDateTime currentTime = LocalDateTime.now();
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setId(sessionId);
        sessionEntity.setCreatedAt(currentTime);
        sessionEntity.setIsActive(true);
        sessionEntity.setLastActiveAt(currentTime);
        sessionEntity.setOfflineSession(true);
        sessionEntity.setIpAddress(ipAddress);
        UserEntity userOfSession = entityManager.getReference(UserEntity.class, userId);
        sessionEntity.setUser(userOfSession);
        sessionRepository.save(sessionEntity);
        return sessionId;
    }

    @Override
    public SessionEntity findSessionActive(String sessionId, boolean active) {
        return sessionRepository.findByIdAndAndIsActive(sessionId, active);
    }
}
