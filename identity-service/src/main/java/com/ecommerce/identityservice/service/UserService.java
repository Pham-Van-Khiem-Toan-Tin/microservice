package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;

import java.util.List;

public interface UserService {
    void register(RegisterForm registerForm);
    void login(LoginForm loginForm);
    List<UserDTO> getAllUser();
}
