package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.IntrospectDTO;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.SessionRepository;
import com.ecommerce.identityservice.repository.TokenRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.SessionService;
import com.ecommerce.identityservice.service.TokenService;
import com.ecommerce.identityservice.service.UserService;
import com.ecommerce.identityservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static com.ecommerce.identityservice.constants.Constants.*;

@Service
public class UserServiceImpl implements UserService {
    @Value("${jwt.accessTokenKey}")
    private String accessTokenKey;
    @Value("${jwt.refreshTokenKey}")
    private String refreshTokenKey;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    SessionService sessionService;
    @Autowired
    TokenService tokenService;
    @Autowired
    EntityManager entityManager;


    @Override
    public UserEntity register(RegisterForm registerForm) throws CustomException {
        if (StringUtils.isEmpty(registerForm.getEmail()) || StringUtils.isEmpty(registerForm.getPassword())
                || StringUtils.isEmpty(registerForm.getFirstName()) || StringUtils.isEmpty(registerForm.getLastName()))
            throw new CustomException(REGISTER_VALIDATE);
        Boolean existsUser = userRepository.existsById(registerForm.getEmail());
        if (existsUser)
            throw new CustomException(EXISTS_USER);
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
        return userRepository.save(newUser);
    }

    @Override
    @Transactional
    public LoginDTO login(LoginForm loginForm, String ipAddress) throws CustomException {
        if (StringUtils.isEmpty(loginForm.getEmail()) || StringUtils.isEmpty(loginForm.getPassword()))
            throw new CustomException(LOGIN_VALIDATE);
        Map<String, Object> userQuery = userRepository.findUserDetailById(loginForm.getEmail());
        if (userQuery == null || userQuery.isEmpty())
            throw new CustomException(LOGIN_NOT_FOUND);
        UserDTO user = UserDTO.from(userQuery);
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword()))
            throw new CustomException(LOGIN_PASS_NOT_MATCH);
        String refreshToken = generateRefreshToken(user.getEmail());
        String sessionId = sessionService.createSession(ipAddress, user.getEmail());
        tokenService.createToken(refreshToken, sessionId);
        String accessToken = generateAccessToken(user, sessionId);
        LoginDTO loginDTO = LoginDTO.from(user);
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);
        return loginDTO;
    }

    @Override
    public IntrospectDTO introspect(String token) throws CustomException {
        IntrospectDTO introspectDTO = new IntrospectDTO();
        try {
            SecretKey secretKey = JwtUtils.getSecretKey(accessTokenKey);
            Claims claims = JwtUtils.claimToken(token, secretKey);
            String sessionId = claims.get("session", String.class);
            SessionEntity session = sessionService.findById(sessionId);

        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            throw new CustomException(TOKEN_EXPIRED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new CustomException(TOKEN_VALIDATE);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new CustomException(TOKEN_VALIDATE);
        }
        return null;
    }

    public String generateRefreshToken(String userId) {
        SecretKey secretKey = JwtUtils.getSecretKey(refreshTokenKey);
        Map<String, Object> claim = new HashMap<>();
        long expiration = System.currentTimeMillis() + 90 * 24 * 60 * 60 * 1000;
        return JwtUtils.generateToken(userId, claim, expiration, secretKey);
    }

    public String generateAccessToken(UserDTO user, String sessionId) {
        SecretKey secretKey = JwtUtils.getSecretKey(accessTokenKey);
        Map<String, Object> claim = new HashMap<>();
        claim.put("role", user.getRole());
        claim.put("functions", user.getFunctions());
        claim.put("subfunctions", user.getSubfunctions());
        claim.put("session", sessionId);
        long expiration = System.currentTimeMillis() + 30 * 1000 * 60;
        return JwtUtils.generateToken(user.getEmail(), claim, expiration, secretKey);
    }

//    @Override
//    public UserDTO getProfile(String token, String userId) {
//
//        UserDTO userDTO = new UserDTO();
//
//        return userDTO;
//    }

    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.substring(0, 1).toUpperCase() + lowerCase.substring(1);
    }
}
