package com.ecommerce.authservice.repository;

import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface FunctionRepositoryCustom {
    Page<FunctionDTO> search(Specification<FunctionEntity> specification, Pageable pageable);
}
