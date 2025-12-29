package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.SubFunctionEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Repository
public interface SubFunctionRepository extends JpaRepository<SubFunctionEntity, UUID>, JpaSpecificationExecutor<SubFunctionEntity> {
    @Query("""
        select s
        from SubFunctionEntity s
        where s.function is null
          and ( lower(s.name) like lower(concat('%', :keyword, '%'))
             or lower(s.code) like lower(concat('%', :keyword, '%')) )
          and ( :excludeIds is null or s.id not in :excludeIds )
        """)
    Set<SubFunctionEntity> searchAvailableByNameOrCode(
            @Param("keyword") String keyword,
            @Param("excludeIds") Set<UUID> excludeIds
    );
    @EntityGraph(attributePaths = {"function"})
    @NonNull
    Set<SubFunctionEntity> findAllByIdIn(Set<UUID> ids);


    Set<SubFunctionEntity> findAllByFunctionIsNullAndIdIn(Collection<UUID> ids);
    @Override
    @EntityGraph(attributePaths = {"function"})
    @NonNull
    Page<SubFunctionEntity> findAll(Specification<SubFunctionEntity> spec, Pageable pageable);

    boolean existsByCode(String code);
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update SubFunctionEntity s
        set s.function = null
        where s.function.id = :functionId
    """)
    void clearFunctionByFunctionId(@io.lettuce.core.dynamic.annotation.Param("functionId") UUID functionId);
}
