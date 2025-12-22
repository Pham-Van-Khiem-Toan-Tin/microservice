package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.entity.FunctionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Set;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionEntity, String> {
    @Query("""
              select distinct f
              from FunctionEntity f
              left join fetch f.subFunctions sf
            """)
    Set<FunctionEntity> findAllWithSubFunctions();
}
