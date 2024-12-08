package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SessionEntity;
import com.ecommerce.identityservice.entity.TokenEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.mapper.UserQueryMapper;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.SessionRepository;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.TokenService;
import com.ecommerce.identityservice.service.AuthService;
import com.ecommerce.identityservice.utils.DateTimeUtils;
import com.ecommerce.identityservice.utils.JwtUtils;
import com.ecommerce.identityservice.utils.ValidateUtils;
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
    SessionRepository sessionRepository;
    @Autowired
    TokenService tokenService;
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
    @Transactional
    public LoginDTO login(LoginForm loginForm, String ipAddress) throws CustomException {
        if (!ValidateUtils.validateEmail(loginForm.getEmail()) || !ValidateUtils.validatePassword(loginForm.getPassword()))
            throw new CustomException(LOGIN_VALIDATE);
        Map<String, Object> userQuery = userRepository.findAuthProfile(loginForm.getEmail());
        if (userQuery == null || userQuery.isEmpty())
            throw new CustomException(LOGIN_NOT_FOUND);
        UserDTO user = UserQueryMapper.toUserDTO(userQuery);
        if (user.getBlock())
            throw new CustomException(LOGIN_BLOCK);
        LocalDateTime unlockTime = user.getUnlockTime();
        LocalDateTime currentTime = LocalDateTime.now();
        if (unlockTime != null && unlockTime.isAfter(currentTime)) {
            throw new CustomException(401, "Tài khoản đang bị khoá. Vui lòng đăng nhập sau " + DateTimeUtils.convertToTimeBetweenString(currentTime, unlockTime));
        }
        if (!passwordEncoder.matches(loginForm.getPassword(), user.getPassword())) {
            Integer loginFailCount = user.getLoginFailCount();
            LocalDateTime newUnlockTime;
            Integer newLoginFailCount;
            if (loginFailCount == 5) {
                newUnlockTime = currentTime.plusHours(12);
                newLoginFailCount = 0;
            } else {
                newLoginFailCount = loginFailCount + 1;
                newUnlockTime = currentTime.plusMinutes(newLoginFailCount);
            }
            userRepository.updateTemporaryLock(user.getEmail(), currentTime, newUnlockTime, newLoginFailCount);
            throw new CustomException(410, "Thông tin email hoặc mật khẩu không đúng. Tài khoản của bạn bị khoá đến " + DateTimeUtils.convertToTimeString(newUnlockTime, "HH:mm:ss dd-MM-yyyy"));
        }
        String refreshToken = generateRefreshToken(user.getEmail());
        String sessionId = UUID.randomUUID().toString();
        String tokenId = tokenService.createToken(refreshToken, sessionId);
        createSession(ipAddress, user.getEmail(), tokenId, sessionId);
        String accessToken = generateAccessToken(user, currentTime);
        LoginDTO loginDTO = LoginDTO.from(user);
        loginDTO.setAccessToken(accessToken);
        loginDTO.setRefreshToken(refreshToken);
        loginDTO.setSessionId(sessionId);
        return loginDTO;
    }

    @Override
    public void logout(String userId, String sessionId) throws CustomException {
        int rowUpdate = sessionRepository.updateEndAtAndActiveById(sessionId, userId, LocalDateTime.now(), false);
        if (rowUpdate == 0)
            throw new CustomException(LOGOUT_ERROR);
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
            Map<String, Object> userQuery = userRepository.findAuthProfile(session.getUser().getEmail());
            if (userQuery == null || userQuery.isEmpty())
                throw new CustomException(LOGIN_EXPIRED);
            UserDTO user = UserQueryMapper.toUserDTO(userQuery);
            String accessToken = generateAccessToken(user, currentTime);
            renewTokenDTO.setAccessToken(accessToken);
            renewTokenDTO.setExpireIn(currentTime.plusMinutes(accessTokenExpired)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toInstant()
                    .toEpochMilli());
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
