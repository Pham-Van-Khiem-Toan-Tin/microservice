package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.UpdateProfileForm;
import com.ecommerce.identityservice.dto.request.UpdateProfileRequest;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.UserProfileDto;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.dto.response.user.UserProfileResponse;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.ecommerce.identityservice.constants.Constants.*;

@RestController
@RequestMapping("/profile")
public class ProfileController {
    @Autowired
    private UserService userService;
    @PreAuthorize("hasAuthority('VIEW_CUSTOMER_PROFILE')")
    @GetMapping
    public ResponseEntity<UserProfileDto> getProfile() {
        return ResponseEntity.ok(userService.getMyProfile());
    }
    @PreAuthorize("hasAuthority('EDIT_CUSTOMER_PROFILE')")
    @PutMapping("/update-all")
    public ApiResponse<Void> update(@ModelAttribute UpdateProfileForm updateProfileForm) {
        userService.updateProfile(updateProfileForm);
        return ApiResponse.ok(PROFILE_UPDATE_SUCCESS);
    }
    @GetMapping("/review")
    public UserResponse getReview() {
        return userService.getUser();
    }
    @GetMapping("/auth")
    public UserProfileResponse getMyProfile() {
        // Lấy ID người dùng từ SecurityContext (đã được Filter giải mã từ JWT)
        return userService.getUserProfile();
    }

    @PutMapping("/auth")
    public ApiResponse<Void> updateMyProfile(@RequestBody UpdateProfileRequest request) {
        userService.updateUserProfile(request);
        return ApiResponse.ok(PROFILE_UPDATE_SUCCESS);
    }
}
