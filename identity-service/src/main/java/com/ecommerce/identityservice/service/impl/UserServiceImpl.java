package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;


@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;

    @Override
    public Boolean register(RegisterForm registerForm) {
        Boolean existing = userRepository.existsById(registerForm.getEmail());
        if (existing)
            return false;
        UserEntity newUser = new UserEntity();
        newUser.setEmail(registerForm.getEmail());
        newUser.setPassword(passwordEncoder.encode(registerForm.getPassword()));
        newUser.setLoginFailCount(0);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setBlock(false);
        RoleEntity defaultRole = roleRepository.findById("CUSTOMER").orElse(null);
        if (defaultRole == null)
            return false;
        newUser.setRole(defaultRole);
        userRepository.save(newUser);
        return true;
    }

    @Override
    public UserDTO getProfile(String token, String userId) {

        UserDTO userDTO = new UserDTO();

        return userDTO;
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.substring(0,1).toUpperCase() + lowerCase.substring(1);
    }
}
