package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.response.AuthResponse;
import com.ecommerce.authservice.dto.response.TokenResponse;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

@RestController
public class AuthController {
    @Autowired
    PasswordEncoder passwordEncoder;
    private static final String IDP_AUTHORIZE_URL = "http://127.0.0.1:8085/oauth2/authorize";
    private static final String CLIENT_ID = "admin-client";
    private static final String REDIRECT_URI = "http://127.0.0.1:8082/auth/admin/callback";
    private String generateStateToken() {
        byte[] bytes = new byte[24]; // 24 bytes ~ 32 ký tự base64
        SecureRandom random = new SecureRandom();
        random.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }
    @PreAuthorize("hasAuthority('VIEW_USER')")
    @GetMapping("/me")
    public ResponseEntity<AuthResponse> authenticatedProfile(@AuthenticationPrincipal Jwt jwt) {
        AuthResponse authResponse = AuthResponse.builder()
                .userId(jwt.getClaim("uid"))
                .userName(jwt.getSubject())
                .build();
        return new ResponseEntity<>(authResponse, HttpStatus.OK);
    }
//    @GetMapping("/login")
//    public void login(HttpServletResponse response) throws IOException {
//        String state = generateStateToken();
//
//        String url = IDP_AUTHORIZE_URL
//                + "?response_type=code"
//                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
//                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
//                + "&scope=" + URLEncoder.encode("openid profile", StandardCharsets.UTF_8)
//                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
//
//        // Trả về 302 Location cho browser → browser tự đi tới IdP
//        response.sendRedirect(url);
//    }
//    @GetMapping("/oauth2/authorization/{clientId}")
//    public void generateStateToLogin(HttpServletResponse response, @PathVariable String clientId) throws IOException {
//        String state = generateStateToken();
//        String url = IDP_AUTHORIZE_URL
//                + "?response_type=code"
//                + "&client_id=" + URLEncoder.encode(CLIENT_ID, StandardCharsets.UTF_8)
//                + "&redirect_uri=" + URLEncoder.encode(REDIRECT_URI, StandardCharsets.UTF_8)
//                + "&scope=" + URLEncoder.encode("openid profile", StandardCharsets.UTF_8)
//                + "&state=" + URLEncoder.encode(state, StandardCharsets.UTF_8);
//        response.sendRedirect(url);
//    }
//    @GetMapping("/admin/callback")
//    public ResponseEntity<String> callback(@RequestParam("code") String code, @RequestParam("state") String state) {
//        String tokenEndpoint = "http://127.0.0.1:8085/oauth2/token";
//        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
//        form.add("grant_type", "authorization_code");
//        form.add("code", code);
//        form.add("redirect_uri", "http://127.0.0.1:8082/auth/admin/callback");
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
//        headers.setBasicAuth("admin-client","secret");
//        RestTemplate restTemplate = new RestTemplate();
//        HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(form, headers);
//        ResponseEntity<TokenResponse> tokenResponse =
//                restTemplate.postForEntity(tokenEndpoint, entity, TokenResponse.class);
//        System.out.println("Status: " + tokenResponse.getStatusCode());
//
//        assert tokenResponse.getBody() != null;
//        System.out.println("Body: " + tokenResponse.getBody().getAccessToken());
//        return ResponseEntity.ok("login success");
//    }
}
