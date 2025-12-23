package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.service.SubFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/subfunctions")
public class SubFunctionController {
    @Autowired
    private SubFunctionService subFunctionService;
    @PreAuthorize("hasAuthority('VIEW_SUBFUNCTION_LIST')")
    @GetMapping("/unlink")
    public Set<SubFunctionDTO> getAllSubFunctions() {
        return subFunctionService.getUnlinkedSubFunctions();
    }
    @PreAuthorize("hasAuthority('VIEW_SUBFUNCTION_LIST')")
    @GetMapping
    public Page<SubFunctionDTO> getAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<String> fields,
            @RequestParam(defaultValue = "id:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return subFunctionService.search(keyword, fields, sort, page, size);
    }
}
