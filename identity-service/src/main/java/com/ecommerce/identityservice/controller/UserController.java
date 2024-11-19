package com.ecommerce.identityservice.controller;

import static com.ecommerce.identityservice.constants.Constants.*;

import com.ecommerce.identityservice.dto.IntrospectDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.LoginDTO;
import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.service.impl.UserServiceImpl;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
public class UserController {
    @Autowired
    UserServiceImpl userService;

    @PostMapping("/account/register")
    public ApiResponse<String> register(@RequestBody RegisterForm registerForm) throws Exception {
        userService.register(registerForm);
        return new ApiResponse<>(REGISTER_SUCCESS);
    }

    @PostMapping("/account/login")
    public ApiResponse<LoginDTO> login(@RequestBody LoginForm loginForm, HttpServletRequest request, HttpServletResponse response) throws CustomException {
        String ipAddress = getClientIP(request);
        LoginDTO loginResponse = userService.login(loginForm, ipAddress);
        addTokenToCookie(loginResponse.getRefreshToken(), response);
        loginResponse.setRefreshToken(null);
        return new ApiResponse<>(200, loginResponse);
    }
    @GetMapping("/account/introspect")
    public ApiResponse<IntrospectDTO> introspect(@RequestBody String token) throws CustomException {
        return new ApiResponse<>(200, userService.introspect(token));
    }
    @GetMapping("/account/basic-profile")
    public ResponseEntity<String> basicProfile() {

        return ResponseEntity.ok("test");
    }

    //    @GetMapping("/profile")
//    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
//    public ResponseEntity<UserDTO> getUserInfo(@AuthenticationPrincipal Jwt jwt, @RequestHeader("Authorization") String token) throws Exception {
//
//        String userId = jwt.getClaim("sub");  // "sub" là userId trong JWT
//        UserDTO user = userService.getProfile(token,userId);
//        RestTemplate restTemplate = new RestTemplate();
//        HttpHeaders httpHeaders = new HttpHeaders();
//        httpHeaders.set(HttpHeaders.AUTHORIZATION, token);
//        String url = "http://localhost:8084/payment/profile";
//        HttpEntity<String> entity = new HttpEntity<>(null, httpHeaders);
//        BillingDTO billing = restTemplate.exchange(url, HttpMethod.GET, entity, BillingDTO.class).getBody();
//        user.setBilling(billing);
//        return ResponseEntity.ok(user);
//    }
    private void addTokenToCookie(String refreshToken, HttpServletResponse response) {
        Cookie refreshTokenCookie = new Cookie("refresh_token", refreshToken);
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(90 * 24 * 60 * 60);
        response.addCookie(refreshTokenCookie);
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

