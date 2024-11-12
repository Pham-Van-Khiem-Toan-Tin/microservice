package com.ecommerce.userservice.service;


import com.ecommerce.userservice.dto.UserDTO;

import java.util.List;

public interface UserService {
    UserDTO getProfile(String token, String userId);
}
