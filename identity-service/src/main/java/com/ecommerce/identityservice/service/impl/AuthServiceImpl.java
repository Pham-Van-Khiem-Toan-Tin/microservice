package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.SessionRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.TokenService;
import com.ecommerce.identityservice.service.AuthService;
import com.ecommerce.identityservice.utils.JwtUtils;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.security.SignatureException;
import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
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
    @Value("${jwt.refreshTokenKey}")
    private String refreshTokenKey;
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    TokenService tokenService;
    @Autowired
    EntityManager entityManager;


    @Override
    public UserEntity register(RegisterForm registerForm) throws CustomException {
        if (!StringUtils.isEmpty(registerForm.getEmail()) || !StringUtils.isEmpty(registerForm.getPassword()))
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
    @Transactional
    public LoginDTO login(LoginForm loginForm, String ipAddress) throws CustomException {
        if (!StringUtils.hasText(loginForm.getEmail()) || !StringUtils.hasText(loginForm.getPassword()))
            throw new CustomException(LOGIN_VALIDATE);
        Map<String, Object> userQuery = userRepository.findUserDetailById(loginForm.getEmail());
        if (userQuery == null || userQuery.isEmpty())
            throw new CustomException(LOGIN_NOT_FOUND);
        UserDTO user = UserDTO.from(userQuery);
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword()))
            throw new CustomException(LOGIN_PASS_NOT_MATCH);
        String refreshToken = generateRefreshToken(user.getEmail());
        String sessionId = UUID.randomUUID().toString();
        String tokenId = tokenService.createToken(refreshToken, sessionId);
        createSession(ipAddress, user.getEmail(), tokenId, sessionId);
        String accessToken = generateAccessToken(user);
        LoginDTO loginDTO = LoginDTO.from(user);
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);
        loginDTO.setSessionId(sessionId);
        return loginDTO;
    }

    @Override
    @Transactional
    public IntrospectDTO introspect(String token, String ipAddress, String sessionId) throws CustomException {
        try {
            if (sessionId == null || !StringUtils.hasText(token))
                throw new CustomException(LOGIN_EXPIRED);
            SecretKey secretKey = JwtUtils.getSecretKey(accessTokenKey);
            Claims claims = JwtUtils.claimToken(token, secretKey);
            IntrospectDTO introspectDTO = IntrospectDTO.from(claims);
            SessionEntity session = sessionRepository.findByIdAndIsActive(sessionId, true);
            if (session == null)
                throw new CustomException(LOGIN_EXPIRED);
            LocalDateTime currentTime = LocalDateTime.now();
            if (session.getLastActiveAt().plusHours(2).isBefore(currentTime)) {
                String newSessionId = UUID.randomUUID().toString();
                createSession(ipAddress, session.getUser().getEmail(), session.getToken().getId(), newSessionId);
                session.setIsActive(false);
                introspectDTO.setSession(newSessionId);
            } else {
                session.setLastActiveAt(LocalDateTime.now());
            }
            sessionRepository.save(session);
            return introspectDTO;
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
    }

    @Override
    @Transactional
    public RenewTokenDTO renewAccessToken(String ipAddress, String sessionId, String refreshToken) throws CustomException {
        try {
            if (!StringUtils.hasText(sessionId) || !StringUtils.hasText(refreshToken))
                throw new CustomException(LOGIN_EXPIRED);
            SecretKey secretKey = JwtUtils.getSecretKey(refreshTokenKey);
            boolean validateToken = JwtUtils.validateToken(refreshToken, secretKey);
            if (!validateToken)
                throw new CustomException(LOGIN_EXPIRED);
            SessionEntity session = sessionRepository.findByIdAndIsActive(sessionId, true);
            if (session == null)
                throw new CustomException(LOGIN_EXPIRED);
            LocalDateTime currentTime = LocalDateTime.now();
            RenewTokenDTO renewTokenDTO = new RenewTokenDTO();
            if (session.getLastActiveAt().plusHours(2).isBefore(currentTime)) {
                String newSessionId = UUID.randomUUID().toString();
                createSession(ipAddress, session.getUser().getEmail(), session.getToken().getId(), newSessionId);
                session.setIsActive(false);
                renewTokenDTO.setSessionId(newSessionId);
            } else {
                session.setLastActiveAt(LocalDateTime.now());
            }
            sessionRepository.save(session);
            Map<String, Object> userQuery = userRepository.findUserDetailById(session.getUser().getEmail());
            if (userQuery == null || userQuery.isEmpty())
                throw new CustomException(LOGIN_EXPIRED);
            UserDTO user = UserDTO.from(userQuery);
            String accessToken = generateAccessToken(user);
            renewTokenDTO.setAccessToken(accessToken);
            return renewTokenDTO;
        } catch (ExpiredJwtException e) {
            e.printStackTrace();
            throw new CustomException(LOGIN_EXPIRED);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            throw new CustomException(TOKEN_VALIDATE);
        } catch (SignatureException e) {
            e.printStackTrace();
            throw new CustomException(TOKEN_VALIDATE);
        }
    }

    public SessionEntity createSession(String ipAddress, String userId, String token, String sessionId) {
        LocalDateTime currentTime = LocalDateTime.now();
        SessionEntity sessionEntity = new SessionEntity();
        sessionEntity.setId(sessionId);
        sessionEntity.setCreatedAt(currentTime);
        sessionEntity.setIsActive(true);
        sessionEntity.setLastActiveAt(currentTime);
        sessionEntity.setOfflineSession(true);
        sessionEntity.setIpAddress(ipAddress);
        sessionEntity.setToken(entityManager.getReference(TokenEntity.class, token));
        UserEntity userOfSession = entityManager.getReference(UserEntity.class, userId);
        sessionEntity.setUser(userOfSession);

        return sessionRepository.save(sessionEntity);
    }

    public String generateRefreshToken(String userId) {
        SecretKey secretKey = JwtUtils.getSecretKey(refreshTokenKey);
        Map<String, Object> claim = new HashMap<>();
        LocalDateTime currentTime = LocalDateTime.now();
        long expiration = currentTime.plusDays(90).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();
        return JwtUtils.generateToken(userId, claim, expiration, secretKey);
    }

    public String generateAccessToken(UserDTO user) {
        SecretKey secretKey = JwtUtils.getSecretKey(accessTokenKey);
        Map<String, Object> claim = new HashMap<>();
        claim.put("role", user.getRole());
        claim.put("functions", user.getFunctions());
        claim.put("subfunctions", user.getSubfunctions());
        LocalDateTime currentTime = LocalDateTime.now();
        long expiration = currentTime.plusMinutes(30).atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toInstant().toEpochMilli();;
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
