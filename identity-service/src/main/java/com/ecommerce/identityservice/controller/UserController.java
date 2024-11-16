package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.form.LoginForm;
import com.ecommerce.identityservice.form.RegisterForm;
import com.ecommerce.identityservice.service.impl.UserServiceImpl;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;



@RestController
public class UserController {
    @Autowired
    UserServiceImpl userService;
    @PostMapping("/account/register")
    public ResponseEntity<String> register(@RequestBody RegisterForm registerForm) {
        if (StringUtils.isEmpty(registerForm.getEmail()) || StringUtils.isEmpty(registerForm.getPassword())
                || StringUtils.isEmpty(registerForm.getFirstName()) || StringUtils.isEmpty(registerForm.getLastName()))
            return ResponseEntity.status(500).body("Dữ liệu không hơp lệ");
        Boolean existsUser = userService.existUser(registerForm.getEmail());
        if (existsUser)
            return ResponseEntity.status(500).body("Email đã tồn tại trong hệ thống");
        Boolean result = userService.register(registerForm);
        if (!result)
            return ResponseEntity.status(500).body("Không thể đăng kí người dùng. Vui lòng thử lại sau");
        return ResponseEntity.ok("Đăng kí người dùng thành công");
    }
    @PostMapping("/account/login")
    public ResponseEntity<String> login(@RequestBody LoginForm loginForm) {
        if (StringUtils.isEmpty(loginForm.getEmail()) || StringUtils.isEmpty(loginForm.getPassword()))
            return ResponseEntity.status(500).body("Thông tin đăng nhập không hợp lệ");

        return null;
    }
    @GetMapping("/test")
    public ResponseEntity<String> test() {
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
}

