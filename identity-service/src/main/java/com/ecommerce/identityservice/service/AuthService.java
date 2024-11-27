package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.IntrospectDTO;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.dto.RenewTokenDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;

public interface AuthService {
    UserEntity register(RegisterForm registerForm) throws CustomException;
    LoginDTO login(LoginForm loginForm, String ipAddress) throws CustomException;
    void logout(String userId, String sessionId) throws CustomException;
    IntrospectDTO introspect(String token, String ipAddress, String sessionId) throws  CustomException;
    RenewTokenDTO renewAccessToken(String ipAddress, String sessionId, String refreshToken) throws CustomException;
    //    UserDTO getProfile(String token, String userId);
}
