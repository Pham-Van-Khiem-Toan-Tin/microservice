package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.AuthService;
import com.ecommerce.identityservice.utils.JwtUtils;
import com.ecommerce.identityservice.utils.ValidateUtils;
import jakarta.persistence.EntityManager;
import org.springframework.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static com.ecommerce.identityservice.constants.Constants.*;

@Service
public class AuthServiceImpl implements AuthService {
    @Value("${jwt.accessTokenKey}")
    private String accessTokenKey;
    @Value("${jwt.accessTokenExpired}")
    private int accessTokenExpired;
    @Value("${jwt.refreshTokenKey}")
    private String refreshTokenKey;
    @Value("${jwt.refreshTokenExpired}")
    private int refreshTokenExpired;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    EntityManager entityManager;


    @Override
    public UserEntity register(RegisterForm registerForm) throws CustomException {
        if (!ValidateUtils.validateEmail(registerForm.getEmail()) || !ValidateUtils.validatePassword(registerForm.getPassword()))
            throw new CustomException(REGISTER_VALIDATE);
        Boolean existsUser = userRepository.existsById(registerForm.getEmail());
        if (existsUser)
            throw new CustomException(EXISTS_USER);
        UserEntity newUser = new UserEntity();
        newUser.setEmail(registerForm.getEmail());
        newUser.setFirstName(Optional.ofNullable(registerForm.getFirstName()).filter(StringUtils::hasText).orElse(null));
        newUser.setLastName(Optional.ofNullable(registerForm.getLastName()).filter(StringUtils::hasText).orElse(null));
        newUser.setPassword(passwordEncoder.encode(registerForm.getPassword()));
        newUser.setLoginFailCount(0);
        newUser.setCreatedAt(LocalDateTime.now());
        newUser.setBlock(false);
        RoleEntity defaultRole = entityManager.getReference(RoleEntity.class, "CUSTOMER");
        newUser.setRole(defaultRole);
        return userRepository.save(newUser);
    }



    @Override
    public void logout(String userId, String sessionId) throws CustomException {
//        int rowUpdate = sessionRepository.updateEndAtAndActiveById(sessionId, userId, LocalDateTime.now(), false);
//        if (rowUpdate == 0)
//            throw new CustomException(LOGOUT_ERROR);
    }



    public String generateRefreshToken(String userId) {
        SecretKey secretKey = JwtUtils.getSecretKey(refreshTokenKey);
        Map<String, Object> claim = new HashMap<>();
        LocalDateTime currentTime = LocalDateTime.now();
        long expiration = currentTime.plusDays(refreshTokenExpired).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
        return JwtUtils.generateToken(userId, claim, expiration, secretKey);
    }

    public String generateAccessToken(UserDTO user, LocalDateTime currentTime) {
        SecretKey secretKey = JwtUtils.getSecretKey(accessTokenKey);
        Map<String, Object> claim = new HashMap<>();
        claim.put("role", user.getRole());
        claim.put("functions", user.getFunctions());
        claim.put("subfunctions", user.getSubfunctions());
        long expiration = currentTime.plusMinutes(accessTokenExpired).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();;
        return JwtUtils.generateToken(user.getEmail(), claim, expiration, secretKey);
    }


    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }
}
