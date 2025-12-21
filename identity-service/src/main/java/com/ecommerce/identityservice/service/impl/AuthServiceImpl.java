package com.ecommerce.identityservice.service.impl;

import static com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.request.RegisterForm;
import com.ecommerce.identityservice.dto.request.ResendOtpForm;
import com.ecommerce.identityservice.dto.request.VerifyForm;
import com.ecommerce.identityservice.dto.response.BusinessException;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.reppository.RoleRepository;
import com.ecommerce.identityservice.reppository.UserRepository;
import com.ecommerce.identityservice.service.AuthService;
import com.ecommerce.identityservice.service.EmailService;
import com.ecommerce.identityservice.utils.OtpUtils;
import jakarta.mail.MessagingException;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EntityManager entityManager;
    private final EmailService emailService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, RoleRepository roleRepository, EntityManager entityManager, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.entityManager = entityManager;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public UserEntity createUser(RegisterForm user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new BusinessException(EXISTS_USER);
        }
        String otp = OtpUtils.generate6Digit();
        RoleEntity role = entityManager.getReference(RoleEntity.class, "CUSTOMER");
        Set<RoleEntity> roles = new HashSet<>();
        roles.add(role);
        UserEntity userEntity = UserEntity
                .builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .password(passwordEncoder.encode(user.getPassword()))
                .verifyEmail(false)
                .status(3)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .otpEmail(otp)
                .otpEmailExpiration(Instant.now().plusSeconds(120))
                .build();
        userEntity.setRoles(roles);
        UserEntity createdUser = userRepository.save(userEntity);
        try {
            emailService.sendOtpEmail(createdUser.getEmail(), createdUser.getFirstName() + createdUser.getLastName(), otp, 2);
        } catch (MessagingException e) {
            throw new RuntimeException(e);
        }
        return createdUser;
    }
    @Transactional
    @Override
    public void verifyUser(VerifyForm verifyForm) {
        UserEntity user = userRepository.findByEmail(verifyForm.getEmail()).orElseThrow(() -> new BusinessException(USER_NOTFOUND));
        if (user.getOtpEmail() == null || user.getOtpEmailExpiration() == null) {
            throw new BusinessException(OTP_EXIST);
        }
        if (!user.getOtpEmail().equals(verifyForm.getOtp())) {
            throw new BusinessException(OTP_FAIL);
        }
        if (user.getOtpEmailExpiration().isBefore(Instant.now())) {
            throw new BusinessException(OTP_EXPIRE);
        }
        user.setOtpEmailExpiration(null);
        user.setOtpEmail(null);
        user.setStatus(1);
        user.setUpdatedAt(Instant.now());
        user.setVerifyEmail(true);
        user.setUpdatedBy(user);
        userRepository.save(user);
    }
    @Transactional
    @Override
    public void resendOtp(ResendOtpForm resendOtpForm) {
        UserEntity user = userRepository.findByEmail(resendOtpForm.getEmail())
                .orElseThrow(() -> new BusinessException(USER_NOTFOUND));
        if (user.getVerifyEmail()) {
            throw new BusinessException(USER_ALREADY_VERIFIED);
        }
        if (user.getOtpEmailExpiration().isAfter(Instant.now())) {
            throw new BusinessException(OTP_EXPIRE);
        }
        String otp = OtpUtils.generate6Digit();
        user.setOtpEmail(otp);
        user.setOtpEmailExpiration(Instant.now().plusSeconds(2 * 60));
        userRepository.save(user);
        try {
            emailService.sendOtpEmail(user.getEmail(), user.getFullName(), otp, 2);
        } catch (MessagingException e) {
            log.error(e.getMessage());
            throw new RuntimeException("Không thể xử lí yêu cầu.");
        }

    }
}
