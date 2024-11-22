package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.SessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SessionRepository extends JpaRepository<SessionEntity, String> {
    @Query(value = "select * from session " +
    "where session.id = :id and session.is_active = :ac", nativeQuery = true)
    SessionEntity findByIdAndAndIsActive(@Param("id") String sessionId, @Param("ac") boolean isActive);

}
