package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.RoleForm;
import com.ecommerce.identityservice.entity.RoleEntity;

public interface RoleService {
    RoleEntity createRole(RoleForm role);
}
