package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.AddressForm;
import com.ecommerce.identityservice.dto.request.AddressUpdateForm;
import com.ecommerce.identityservice.dto.request.UpdateProfileForm;
import com.ecommerce.identityservice.dto.request.UpdateProfileRequest;
import com.ecommerce.identityservice.dto.response.UserProfileDto;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.dto.response.user.UserProfileResponse;
import com.ecommerce.identityservice.dto.response.user.UserSummaryResponse;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<RoleEntity> getAllRoles();

    void updateUserRole(String userId, String roleId);
    void updateUserStatus(String userId, int status, UserEntity admin);
    Page<UserSummaryResponse> getAllUsers(String keyword, Integer status, Pageable pageable);
    UserProfileDto getMyProfile();
    void updateProfile(UpdateProfileForm updateProfileForm);
    void addAddress(AddressForm form);
    void updateAddress(String addressId, AddressUpdateForm form);
    void deleteAddress(String addressId);
    UserResponse getUser();
    UserProfileResponse getUserProfile();
    void updateUserProfile(UpdateProfileRequest request);
}
