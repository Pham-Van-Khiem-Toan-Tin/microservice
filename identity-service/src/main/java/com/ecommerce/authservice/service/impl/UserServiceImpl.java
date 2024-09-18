package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.model.UserEntity;
import com.ecommerce.authservice.repository.UserRepository;
import com.ecommerce.authservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Override
    public void register(UserEntity user) {
       if (userRepository.existsByEmail(user.getEmail())) {
           throw new RuntimeException("User already exist");
       }
       user.setId(user.getEmail());
       user.setPassword(passwordEncoder.encode(user.getPassword()));
       userRepository.save(user);
    }
}
