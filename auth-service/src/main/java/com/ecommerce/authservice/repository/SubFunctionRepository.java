package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;

@Repository
public interface SubFunctionRepository extends JpaRepository<SubFunctionEntity, String>, JpaSpecificationExecutor<SubFunctionEntity> {
    @Query("""
            select s
            from SubFunctionEntity s
            where s.function is null 
            and (
            :keyword is null or :keyword = '' or
            lower(s.name) like lower(concat('%', :keyword, '%')) or
                  lower(s.id) like lower(concat('%', :keyword, '%'))
            )
            and (:excludeIds is null or s.id not in :excludeIds)
            """)
    Set<SubFunctionEntity> searchByNameOrIdFunctionNullExcludeIds( @Param("keyword") String keyword,
                                                                   @Param("excludeIds") Set<String> excludeIds);
    @EntityGraph(attributePaths = {"function"})
    @NonNull
    Set<SubFunctionEntity> findAllByIdIn(Set<String> ids);


    Set<SubFunctionEntity> findAllByFunctionIsNullAndIdIn(Collection<String> ids);
    @Override
    @EntityGraph(attributePaths = {"function"})
    @NonNull
    Page<SubFunctionEntity> findAll(Specification<SubFunctionEntity> spec, Pageable pageable);
}
