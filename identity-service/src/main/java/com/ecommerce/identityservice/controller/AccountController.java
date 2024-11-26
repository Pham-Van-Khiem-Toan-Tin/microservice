package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.ProfileDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
public class AccountController {
    @Autowired
    private UserService userService;
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public ApiResponse<ProfileDTO> profile() throws CustomException {
        String userid = SecurityContextHolder.getContext().getAuthentication().getName();
        return new ApiResponse<>(200, userService.getProfile(userid));
    }
}
