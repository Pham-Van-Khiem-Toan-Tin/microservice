package com.ecommerce.identityservice.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/account")
@RestController
public class UserController {
    @PreAuthorize("hasAuthority('VIEW_PROFILE')")
    @GetMapping("/profile")
    public String profile() {
        return "test";
    }
}
