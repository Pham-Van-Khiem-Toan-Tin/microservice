package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import jakarta.persistence.EntityManager;
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
    @Autowired
    EntityManager entityManager;
    @Override
    public Boolean register(RegisterForm registerForm) {
        try {
            UserEntity newUser = new UserEntity();
            newUser.setEmail(registerForm.getEmail());
            newUser.setFirstName(registerForm.getFirstName());
            newUser.setLastName(registerForm.getLastName());
            newUser.setPassword(passwordEncoder.encode(registerForm.getPassword()));
            newUser.setLoginFailCount(0);
            newUser.setCreatedAt(LocalDateTime.now());
            newUser.setBlock(false);
            RoleEntity defaultRole = entityManager.getReference(RoleEntity.class, "CUSTOMER");
            newUser.setRole(defaultRole);
            userRepository.save(newUser);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Boolean existUser(String email) {
        return userRepository.existsById(email);
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
