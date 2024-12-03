package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.UpdateProfileForm;

public interface UserService {
    ProfileDTO getProfile() throws CustomException;

    ProfileDetailDTO getProfileDetail() throws CustomException;

    void updateProfile(UpdateProfileForm form) throws CustomException;

    AuthProfileDTO getAuthProfile() throws CustomException;
    TestDTO test();
}
