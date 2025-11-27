package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.UserForm;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class AuthController {
    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new UserForm());
        return "register";
    }
//    @PostMapping("/register")
//    public String register(@ModelAttribute("user") UserForm userForm) {
//        return "register";
//    }
}
