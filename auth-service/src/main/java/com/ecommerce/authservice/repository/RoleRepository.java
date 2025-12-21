package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<RoleEntity, String> {
    RoleEntity findByName(String name);
    @Query(value = """
        select new com.ecommerce.authservice.dto.response.RoleDTO(
            r.id,
            r.name,
            r.description,
            count(s) as quantityPermission
        )
        from RoleEntity r
        left join r.subFunctions s
        where (:keyword = '' or
               lower(r.id) like concat('%', lower(:keyword), '%')
            or lower(r.name) like concat('%', lower(:keyword), '%'))
        group by r.id, r.name, r.description
    """,
            countQuery = """
        select count(r) from RoleEntity r
        where (:keyword = '' or
               lower(r.id) like concat('%', lower(:keyword), '%')
            or lower(r.name) like concat('%', lower(:keyword), '%'))
    """)
    Page<RoleDTO> search(@Param("keyword") String keyword, Pageable pageable);
}
