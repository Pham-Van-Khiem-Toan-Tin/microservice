package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.request.UserStatusRequest;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.user.UserSummaryResponse;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserService userService;
    @GetMapping
    public Page<UserSummaryResponse> getUsers(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Integer status,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.getAllUsers(q, status, pageable);
    }
    @GetMapping("/roles")
    public List<RoleEntity> getRoles() {
        return userService.getAllRoles();
    }
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> toggleStatus(
            @PathVariable String id,
            @RequestBody UserStatusRequest request,
            @AuthenticationPrincipal UserEntity admin) { // Lấy admin đang đăng nhập
        userService.updateUserStatus(id, request.getStatus(), admin);
        return ApiResponse.ok(Constants.UPDATE_ROLE_SUCCESS);
    }
    @PatchMapping("/{id}/role")
    public ApiResponse<Void> updateRole(
            @PathVariable String id,
            @RequestParam String roleId) {

        userService.updateUserRole(id, roleId);
        return ApiResponse.ok(Constants.UPDATE_ROLE_SUCCESS);
    }
}
