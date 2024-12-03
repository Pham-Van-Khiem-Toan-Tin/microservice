package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.SessionEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    SessionEntity findByIdAndIsActive(String id, boolean isActive);
    @Modifying
    @Transactional
    @Query(value = "UPDATE session SET session_end_at = :endAt, is_active = :isActive WHERE id = :id AND user_id = :usId", nativeQuery = true)
    int updateEndAtAndActiveById(@Param("id") String id, @Param("usId") String userId, @Param("endAt") LocalDateTime endAt, @Param("isActive") boolean isActive);
}
