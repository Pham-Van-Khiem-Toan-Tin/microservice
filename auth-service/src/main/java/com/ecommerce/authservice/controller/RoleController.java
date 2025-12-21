package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.entity.RoleEntity;
import com.ecommerce.authservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RequestMapping("/roles")
@RestController
public class RoleController {
    @Autowired
    private RoleService roleService;
    @PreAuthorize("hasAuthority('VIEW_ROLE_LIST')")
    @GetMapping
    public Set<RoleEntity> findAll() {
        return roleService.findAllRole("");
    }
}
