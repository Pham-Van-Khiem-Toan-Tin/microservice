package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    SessionEntity findByIdAndIsActive(String id, boolean isActive);
}
