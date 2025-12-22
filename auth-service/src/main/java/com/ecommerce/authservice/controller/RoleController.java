package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.request.RoleForm;
import com.ecommerce.authservice.dto.response.RoleDTO;

import com.ecommerce.authservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RequestMapping("/roles")
@RestController
public class RoleController {
    @Autowired
    private RoleService roleService;
    @PreAuthorize("hasAuthority('VIEW_ROLE_LIST')")
    @GetMapping
    public Page<RoleDTO> findAll(@RequestParam(defaultValue = "") String keyword,
                                 @RequestParam(defaultValue = "") int page,
                                 @RequestParam(defaultValue = "") int size) {
        return roleService.search(keyword, PageRequest.of(page, size));
    }
    @PreAuthorize("hasAuthority('CREATE_ROLE')")
    @PostMapping("/new")
    public ResponseEntity<String> createRole(@RequestBody RoleForm roleForm) {
        roleService.createRole(roleForm);
        return ResponseEntity.ok("Tạo mới quyền thành công");
    }

}
