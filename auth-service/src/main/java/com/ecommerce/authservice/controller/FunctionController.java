package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Set;

@RestController
@RequestMapping("/functions")
public class FunctionController {
    @Autowired
    private FunctionService functionService;
    @PreAuthorize("hasAuthority('VIEW_FUNCTION_LIST')")
    @GetMapping("/all")
    public Set<AllFunctionDTO> findAll() {
        return functionService.findAllFunction();
    }
}
