package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.FunctionEntity;
import lombok.NonNull;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface FunctionRepository extends JpaRepository<FunctionEntity, UUID>, JpaSpecificationExecutor<FunctionEntity>, FunctionRepositoryCustom {
    @Query("""
              select distinct f
              from FunctionEntity f
              left join fetch f.subFunctions sf
            """)
    Set<FunctionEntity> findAllWithSubFunctions();
    @Override
    @EntityGraph(attributePaths = {"subFunctions"})
    @NonNull
    Optional<FunctionEntity> findById(@NonNull UUID id);

}
