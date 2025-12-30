package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.FunctionEditForm;
import com.ecommerce.identityservice.dto.request.FunctionForm;
import com.ecommerce.identityservice.dto.response.AllFunctionDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.FunctionDTO;
import com.ecommerce.identityservice.dto.response.FunctionDetailDTO;
import com.ecommerce.identityservice.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

import static com.ecommerce.identityservice.constants.Constants.*;

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
            @RequestParam(defaultValue = "code:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return functionService.search(keyword, fields, sort, page, size);
    }
    @PreAuthorize("hasAuthority('VIEW_FUNCTION')")
    @GetMapping("/{id}")
    public FunctionDetailDTO view(@PathVariable String id) {
        return functionService.findFunctionById(id);
    }
    @PreAuthorize("hasAuthority('CREATE_FUNCTION')")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody FunctionForm functionForm) {
        functionService.createFunction(functionForm);
        return ApiResponse.ok(CREAT_FUNCTION_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_FUNCTION')")
    @PutMapping("/{id}")
    public ApiResponse<Void> edit(@RequestBody FunctionEditForm functionForm, @PathVariable String id ) {
        functionService.editFunction(functionForm, id);
        return ApiResponse.ok(UPDATE_FUNCTION_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_FUNCTION')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        functionService.deleteFunction(id);
        return ApiResponse.ok(DELETE_FUNCTION_SUCCESS);
    }
}
