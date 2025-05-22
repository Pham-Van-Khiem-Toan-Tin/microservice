package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import com.ecommerce.identityservice.form.AuthoritiesWithParentForm;
import com.ecommerce.identityservice.service.AuthoritiesService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin")
public class AuthoritiesController {
    @Autowired
    AuthoritiesService authoritiesService;
    @PostMapping("/role/create")
    public ApiResponse<String> createRole(@Valid @RequestBody AuthoritiesForm form) throws CustomException {
        authoritiesService.createRole(form);
        return new ApiResponse<>(200, "Tạo quyền thành công");
    }
    @PostMapping("/function/create")
    public ApiResponse<String> createFunction(@Valid @RequestBody AuthoritiesForm form) throws CustomException {
        authoritiesService.createFunction(form);
        return new ApiResponse<>(200, "Tạo chức năng thành công");
    }
    @PostMapping("/subfunction/create")
    public ApiResponse<String> createSubFunction(@Valid @RequestBody AuthoritiesWithParentForm form) throws CustomException {
        authoritiesService.createSubFunction(form);
        return new ApiResponse<>(200, "Tạo chức năng thành công");
    }
}
