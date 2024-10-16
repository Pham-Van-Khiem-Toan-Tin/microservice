package com.ecommerce.userservice.service.impl;

import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.entity.User;
import com.ecommerce.userservice.form.LoginForm;
import com.ecommerce.userservice.form.RegisterForm;
import com.ecommerce.userservice.repository.UserRepository;
import com.ecommerce.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Override
    public void register(RegisterForm registerForm) {
       if (userRepository.existsById(registerForm.getEmail())) {
           throw new RuntimeException("User already exist");
       }
        User user = new User();
        user.setId(registerForm.getEmail());
        user.setName(registerForm.getName());
        user.setEmail(registerForm.getEmail());
        user.setPassword(passwordEncoder.encode(registerForm.getPassword()));
       userRepository.save(user);
    }

    @Override
    public void login(LoginForm loginForm) {

    }

    @Override
    public List<UserDTO> getAllUser() {
        List<User> users = userRepository.findAll();
        return users.stream().map(user -> UserDTO.from(user)).collect(Collectors.toList());
    }
}
