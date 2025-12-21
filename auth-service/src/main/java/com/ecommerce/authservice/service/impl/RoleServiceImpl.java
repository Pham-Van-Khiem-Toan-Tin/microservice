package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.entity.RoleEntity;
import com.ecommerce.authservice.repository.RoleRepository;
import com.ecommerce.authservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;
    @Override
    public Set<RoleEntity> findAllRole(String keyword) {
        return new HashSet<>(roleRepository.findAll());
    }
}
