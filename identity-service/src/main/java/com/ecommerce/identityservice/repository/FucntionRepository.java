package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface FucntionRepository extends JpaRepository<FunctionEntity, String> {
    @Query("select count(f) > 0 from FunctionEntity f where f.name = :name and f.client.clientId = :clientId")
    boolean existsByNameAndClientId(@Param("name") String name, @Param("clientId") String clientId);
}
