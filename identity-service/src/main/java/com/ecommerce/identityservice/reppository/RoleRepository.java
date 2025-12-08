package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    boolean existsByIdAndName(String id, String name);
}
