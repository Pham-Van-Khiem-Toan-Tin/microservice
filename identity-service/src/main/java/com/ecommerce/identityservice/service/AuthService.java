package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.IntrospectDTO;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.dto.RenewTokenDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;

public interface AuthService {
    UserEntity register(RegisterForm registerForm) throws CustomException;
    void logout(String userId, String sessionId) throws CustomException;
}
