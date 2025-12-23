package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.entity.SubFunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;
import java.util.Set;

@Repository
public interface SubFunctionRepository extends JpaRepository<SubFunctionEntity, String>, JpaSpecificationExecutor<SubFunctionEntity> {
    Set<SubFunctionEntity> findByFunctionIsNull();
}
