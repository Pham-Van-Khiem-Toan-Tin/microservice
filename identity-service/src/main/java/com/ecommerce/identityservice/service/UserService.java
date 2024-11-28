package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.BillingDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.TestDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.AuthProfileDTO;

public interface UserService {
    ProfileDetailDTO getProfile() throws CustomException;
    AuthProfileDTO getAuthProfile() throws CustomException;
    TestDTO test();
}
