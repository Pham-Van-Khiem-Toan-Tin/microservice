package com.ecommerce.identityservice.controller;

import static com.ecommerce.identityservice.constants.Constants.*;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.request.RegisterForm;
import com.ecommerce.identityservice.dto.request.ResendOtpForm;
import com.ecommerce.identityservice.dto.request.UserForm;
import com.ecommerce.identityservice.dto.request.VerifyForm;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.service.AuthService;
import jakarta.mail.MessagingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }
    @GetMapping("/login")
    public String login(Model model, HttpServletRequest request, HttpServletResponse response) {
        HttpSessionRequestCache cache = new HttpSessionRequestCache();
        SavedRequest saved = cache.getRequest(request, response);
        System.out.println(saved);
        return "login";
    }
    @GetMapping("/terms")
    public String terms() {
        return "terms";
    }
    @GetMapping("/privacy")
    public String privacy() {
        return "privacy";
    }
    @GetMapping("/forgot-password")
    public String forgotPassword() {
        return "forgot-password";
    }
    @GetMapping("/verify-email")
    public String verifyEmail() {
        return "verify-email";
    }
    @GetMapping("/new-password")
    public String newPassword() {
        return "new-password";
    }
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<String>> register(@RequestBody RegisterForm registerForm) {
        UserEntity userEntity = authService.createUser(registerForm);
        return ResponseEntity.ok(ApiResponse.success(userEntity.getEmail()));
    }
    @PostMapping("/resend-otp")
    public ResponseEntity<ApiResponse<String>> resendOtp(@RequestBody ResendOtpForm resendOtpForm) throws MessagingException {
        authService.resendOtp(resendOtpForm);
        return ResponseEntity.ok(ApiResponse.success(RESEND_OTP_SUCCESS));
    }
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Constants>> verifyEmail(@RequestBody VerifyForm verifyForm) {
        authService.verifyUser(verifyForm);
        return ResponseEntity.ok(ApiResponse.success(REGISTER_SUCCESS));
    }
}
