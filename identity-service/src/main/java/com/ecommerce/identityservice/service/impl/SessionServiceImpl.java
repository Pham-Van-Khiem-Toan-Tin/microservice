package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
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

}
