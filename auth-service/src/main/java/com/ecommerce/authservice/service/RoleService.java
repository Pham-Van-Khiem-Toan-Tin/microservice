package com.ecommerce.authservice.service;


import com.ecommerce.authservice.entity.RoleEntity;

import java.util.Set;

public interface RoleService {
    Set<RoleEntity> findAllRole(String keyword);
}
