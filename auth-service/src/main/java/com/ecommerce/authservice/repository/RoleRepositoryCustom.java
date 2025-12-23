package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleRepositoryCustom {
    Page<RoleDTO> search(Specification<RoleEntity> specification, Pageable pageable);
}
