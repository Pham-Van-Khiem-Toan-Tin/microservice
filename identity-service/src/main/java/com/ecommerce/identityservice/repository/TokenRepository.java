package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.TokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TokenRepository extends JpaRepository<TokenEntity, Long> {
}
