package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.RegisterForm;
import com.ecommerce.identityservice.dto.request.UserForm;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
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
    @GetMapping("/new-password")
    public String newPassword() {
        return "new-password";
    }
    @PostMapping("/register")
    public ResponseEntity<String> register(@ModelAttribute("user") UserForm userForm) {
        return ResponseEntity.ok("success");
    }
}
