package com.ecommerce.authservice.controller;

import static com.ecommerce.authservice.constant.Constants.*;

import com.ecommerce.authservice.dto.request.RoleCreateForm;
import com.ecommerce.authservice.dto.request.RoleEditForm;
import com.ecommerce.authservice.dto.response.ApiResponse;
import com.ecommerce.authservice.dto.response.RoleDTO;

import com.ecommerce.authservice.dto.response.RoleDetailDTO;
import com.ecommerce.authservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RequestMapping("/roles")
@RestController
public class RoleController {
    @Autowired
    private RoleService roleService;
    @PreAuthorize("hasAuthority('VIEW_ROLE_LIST')")
    @GetMapping
    public Page<RoleDTO> findAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<String> fields,
            @RequestParam(defaultValue = "id:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return roleService.search(keyword, fields, sort, page, size);
    }
    @PreAuthorize("hasAuthority('VIEW_ROLE')")
    @GetMapping("/{id}")
    public RoleDetailDTO view(@PathVariable String id) {
        return roleService.findById(id);
    }
    @PreAuthorize("hasAuthority('CREATE_ROLE')")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody RoleCreateForm roleForm) {
        roleService.createRole(roleForm);
        return ApiResponse.ok(CREATE_ROLE_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_ROLE')")
    @PutMapping("/{id}")
    public ApiResponse<Void> edit(@PathVariable String id, @RequestBody RoleEditForm roleForm) {
        roleService.updateRole(roleForm, id);
        return ApiResponse.ok(UPDATE_ROLE_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_ROLE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        roleService.deleteRole(id);
        return ApiResponse.ok(DELETE_ROLE_SUCCESS);
    }
}
