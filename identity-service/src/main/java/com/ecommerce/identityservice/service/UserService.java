package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;

import java.util.Optional;

public interface UserService {
    UserEntity register(RegisterForm registerForm) throws CustomException;
    LoginDTO login(LoginForm loginForm, String ipAddress) throws CustomException;
//    UserDTO getProfile(String token, String userId);
}
