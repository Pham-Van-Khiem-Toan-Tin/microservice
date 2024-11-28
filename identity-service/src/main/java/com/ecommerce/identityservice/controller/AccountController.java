package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.TestDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @Autowired
    private UserService userService;
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/base")
    public ApiResponse<AuthProfileDTO> base() throws CustomException {
        return new ApiResponse<>(200, userService.getAuthProfile());
    }
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public ApiResponse<ProfileDetailDTO> profile(HttpServletRequest request) throws CustomException {
        return new ApiResponse<>(200, userService.getProfile());
    }
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/test")
    public ApiResponse<TestDTO> test() throws CustomException {
        return new ApiResponse<>(200, userService.test());
    }

}
