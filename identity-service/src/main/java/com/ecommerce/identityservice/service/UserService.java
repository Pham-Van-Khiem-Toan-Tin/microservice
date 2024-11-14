package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.UserDTO;

public interface UserService {
    UserDTO getProfile(String token, String userId);
}
