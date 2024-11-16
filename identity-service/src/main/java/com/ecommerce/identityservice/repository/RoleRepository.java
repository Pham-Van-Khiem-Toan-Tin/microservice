package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleEntity, String> {
}
