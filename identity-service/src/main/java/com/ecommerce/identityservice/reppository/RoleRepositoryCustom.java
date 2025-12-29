package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.dto.response.RoleDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface RoleRepositoryCustom {
    Page<RoleDTO> search(Specification<RoleEntity> specification, Pageable pageable);
}
