package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    RoleEntity findByName(String name);
}
