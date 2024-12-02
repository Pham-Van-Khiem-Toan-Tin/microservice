package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.dto.exception.CustomException;

public interface UserService {
    ProfileDTO getProfile() throws CustomException;

    ProfileDetailDTO getProfileDetail() throws CustomException;

    AuthProfileDTO getAuthProfile() throws CustomException;
    TestDTO test();
}
