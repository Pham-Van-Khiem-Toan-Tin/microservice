package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.form.RegisterForm;

public interface UserService {
    Boolean register(RegisterForm registerForm);
    Boolean existUser(String email);
    UserDTO getProfile(String token, String userId);
}
