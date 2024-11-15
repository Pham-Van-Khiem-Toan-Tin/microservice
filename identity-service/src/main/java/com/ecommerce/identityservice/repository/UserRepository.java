package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, String> {
}
