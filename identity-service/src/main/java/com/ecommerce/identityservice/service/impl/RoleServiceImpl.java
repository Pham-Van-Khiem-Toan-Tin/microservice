package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.dto.request.RoleForm;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.reppository.RoleRepository;
import com.ecommerce.identityservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;
    @Override
    public RoleEntity createRole(RoleForm role) {
        if (roleRepository.existsByIdAndName(role.getName(), role.getName())) {
            throw new RuntimeException("Role already exists");
        }
        RoleEntity roleEntity = RoleEntity.builder()
                .id(role.getId())
                .name(role.getName())
                .description(role.getDescription())
                .position(role.getPosition())
                .build();

        return roleRepository.save(roleEntity);
    }
}
