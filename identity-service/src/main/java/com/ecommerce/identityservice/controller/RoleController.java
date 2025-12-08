package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.RoleForm;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/role")
public class RoleController {
    @Autowired
    RoleService roleService;

    @PostMapping("/create")
    public String createRole(@RequestBody RoleForm role) {
        RoleEntity roleEntity = roleService.createRole(role);
        if (roleEntity == null) {
            return "error";
        }
        return "success";
    }
    @GetMapping("/test")
    public String test() {
        return "success";
    }
}
