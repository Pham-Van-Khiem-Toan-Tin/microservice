package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.RegisterForm;
import com.ecommerce.identityservice.dto.request.ResendOtpForm;
import com.ecommerce.identityservice.dto.request.VerifyForm;
import com.ecommerce.identityservice.entity.UserEntity;
import jakarta.mail.MessagingException;

public interface AuthService {
    UserEntity createUser(RegisterForm user);
    void verifyUser(VerifyForm verifyForm);
    void resendOtp(ResendOtpForm resendOtpForm) throws MessagingException;
}
