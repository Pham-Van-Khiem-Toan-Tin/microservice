package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import io.lettuce.core.dynamic.annotation.Param;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionEntity, String>, JpaSpecificationExecutor<FunctionEntity>, FunctionRepositoryCustom {
    @Query("""
              select distinct f
              from FunctionEntity f
              left join fetch f.subFunctions sf
            """)
    Set<FunctionEntity> findAllWithSubFunctions();
    @Override
    @EntityGraph(attributePaths = {"subFunctions"})
    @NonNull
    Optional<FunctionEntity> findById(@NonNull String id);

}
