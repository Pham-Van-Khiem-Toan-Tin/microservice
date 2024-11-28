package com.ecommerce.identityservice.controller;

import static com.ecommerce.identityservice.constants.Constants.*;

import com.ecommerce.identityservice.dto.IntrospectDTO;
import com.ecommerce.identityservice.dto.RenewTokenDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.form.IntrospectForm;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.service.impl.AuthServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/auth")
public class AuthController {
    @Autowired
    AuthServiceImpl authService;

    @PostMapping("/register")
    public ApiResponse<String> register(@RequestBody RegisterForm registerForm) throws Exception {
        authService.register(registerForm);
        return new ApiResponse<>(REGISTER_SUCCESS);
    }

    @PostMapping("/login")
    public ApiResponse<LoginDTO> login(@RequestBody LoginForm loginForm, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        String ipAddress = getClientIP(request);
        LoginDTO loginResponse = authService.login(loginForm, ipAddress);
        addTokenToCookie(loginResponse.getRefreshToken(), "refresh_token", response);
        addTokenToCookie(loginResponse.getSessionId(), "session_id", response);
        loginResponse.setRefreshToken(null);
        loginResponse.setSessionId(null);
        return new ApiResponse<>(200, loginResponse);
    }
    @GetMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request, HttpServletResponse response) throws CustomException {
        String userid = SecurityContextHolder.getContext().getAuthentication().getName();
        String sessionId = getCookieValue(request, "session_id");
        authService.logout(userid, sessionId);
        deleteAllCookie(request, response);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/introspect")
    public ApiResponse<IntrospectDTO> introspect(@RequestBody IntrospectForm introspectForm, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        String ipAddress = getClientIP(request);
        IntrospectDTO introspectDTO = authService.introspect(introspectForm.getToken(), ipAddress, introspectForm.getSessionId());
        if (StringUtils.hasText(introspectDTO.getSession())) {
            addTokenToCookie(introspectDTO.getSession(), "session_id", response);
            introspectDTO.setSession(null);
        }
        return new ApiResponse<>(200, introspectDTO);
    }

    @GetMapping("/token")
    public ApiResponse<String> renewToken(HttpServletRequest request, HttpServletResponse response) throws CustomException {
        String ipAddress = getClientIP(request);
        String refreshToken = getCookieValue(request, "refresh_token");
        String sessionId = getCookieValue(request, "session_id");
        RenewTokenDTO renewTokenDTO = authService.renewAccessToken(ipAddress, sessionId, refreshToken);
        if (StringUtils.hasText(renewTokenDTO.getSessionId())) {
            addTokenToCookie(renewTokenDTO.getSessionId(), "session_id", response);
            renewTokenDTO.setSessionId(null);
        }
        return new ApiResponse<>(200, renewTokenDTO.getAccessToken());
    }

    private void deleteAllCookie(HttpServletRequest request, HttpServletResponse response) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                Cookie cookieToDelete = new Cookie(cookie.getName(), null);
                cookieToDelete.setPath("/");
                cookieToDelete.setMaxAge(0);
                response.addCookie(cookieToDelete);
            }
        }
    }
    private String getCookieValue(HttpServletRequest request, String cookieName) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(cookieName)) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    private void addTokenToCookie(String value, String name, HttpServletResponse response) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(true);
        cookie.setPath("/");
        cookie.setMaxAge(90 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    private String getClientIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("X-Real-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        // X-Forwarded-For có thể chứa nhiều địa chỉ IP, lấy địa chỉ đầu tiên.
        if (ipAddress != null && ipAddress.contains(",")) {
            ipAddress = ipAddress.split(",")[0].trim();
        }
        return ipAddress;
    }
}

