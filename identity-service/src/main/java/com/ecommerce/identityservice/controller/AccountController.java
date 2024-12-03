package com.ecommerce.identityservice.controller;

import static com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.ProfileDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.form.UpdateProfileForm;
import com.ecommerce.identityservice.service.UserService;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

@RestController
@RequestMapping("/account")
@Slf4j
public class AccountController {
    @Autowired
    private UserService userService;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    CircuitBreakerRegistry circuitBreakerRegistry;
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/base")
    public ApiResponse<AuthProfileDTO> base() throws CustomException {
        return new ApiResponse<>(200, userService.getAuthProfile());
    }
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public ApiResponse<ProfileDTO> profile() throws CustomException {
        return new ApiResponse<>(200, userService.getProfile());
    }

    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile-detail")
    public ApiResponse<ProfileDetailDTO> profileDetail() throws CustomException {
        return new ApiResponse<>(200, userService.getProfileDetail());
    }
    @PreAuthorize("hasAuthority('EDIT_PROFILE')")
    @PutMapping("/profile-detail")
    public ApiResponse<String> updateProfile(@RequestBody UpdateProfileForm form) throws CustomException {
        userService.updateProfile(form);
        return new ApiResponse<>(PROFILE_UPDATE_SUCCESS);
    }
}
