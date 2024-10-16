package com.ecommerce.userservice.service;

import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.form.LoginForm;
import com.ecommerce.userservice.form.RegisterForm;

import java.util.List;

public interface UserService {
    void register(RegisterForm registerForm);
    void login(LoginForm loginForm);
    List<UserDTO> getAllUser();
}
