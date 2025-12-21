package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.request.RoleForm;
import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.Set;

public interface RoleService {
    Page<RoleDTO> search(String keyword, Pageable pageable);
    RoleEntity createRole(RoleForm roleForm);
}
