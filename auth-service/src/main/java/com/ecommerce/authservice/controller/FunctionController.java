package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/functions")
public class FunctionController {
    @Autowired
    private FunctionService functionService;


    @PreAuthorize("hasAuthority('VIEW_FUNCTION_LIST')")
    @GetMapping("/all")
    public Set<AllFunctionDTO> getAllFunctions() {
        return functionService.findAllFunction();
    }
    @GetMapping("/options")
    public Set<FunctionDTO> getAllFunctionsOptions() {
        return functionService.findAllFunctionsOptions();
    }
    @PreAuthorize("hasAuthority('VIEW_FUNCTION_LIST')")
    @GetMapping
    public Page<FunctionDTO> all(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<String> fields,
            @RequestParam(defaultValue = "id:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return functionService.search(keyword, fields, sort, page, size);
    }
}
