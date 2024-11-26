package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.ProfileDTO;

public interface UserService {
    ProfileDTO getProfile(String userId) throws CustomException;
}
