package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.RoleEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    @Query("select count(r) > 0 from RoleEntity r where r.roleId = :id and r.client.clientId = :clientId")
    boolean existsByNameAndClientId(@Param("id") String id, @Param("clientId") String clientId);

}
