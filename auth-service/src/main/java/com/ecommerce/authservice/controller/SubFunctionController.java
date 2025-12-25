package com.ecommerce.authservice.controller;

import com.ecommerce.authservice.constant.Constants;
import com.ecommerce.authservice.dto.request.SubFunctionForm;
import com.ecommerce.authservice.dto.request.SubFunctionOptionForm;
import com.ecommerce.authservice.dto.response.ApiResponse;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.service.SubFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import static com.ecommerce.authservice.constant.Constants.*;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/subfunctions")
public class SubFunctionController {
    @Autowired
    private SubFunctionService subFunctionService;
    @PreAuthorize("hasAuthority('VIEW_SUBFUNCTION_LIST')")
    @PostMapping("/list/options")
    public Set<SubFunctionDTO> all(@RequestBody SubFunctionOptionForm  subFunctionOptionForm) {
        return subFunctionService.getUnlinkedSubFunctions(subFunctionOptionForm);
    }
    @PreAuthorize("hasAuthority('VIEW_SUBFUNCTION_LIST')")
    @GetMapping
    public Page<SubFunctionDTO> getSubFunctions(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<String> fields,
            @RequestParam(defaultValue = "id:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return subFunctionService.search(keyword, fields, sort, page, size);
    }
    @PreAuthorize("hasAuthority('VIEW_SUBFUNCTION')")
    @GetMapping("/{id}")
    public SubFunctionDTO getSubFunction(@PathVariable String id) {
            return  subFunctionService.getSubFunction(id);
    }
    @PreAuthorize("hasAuthority('CREATE_SUBFUNCTION')")
    @PostMapping
    public ApiResponse<Void> add(@RequestBody SubFunctionForm form) {
        subFunctionService.createSubFunction(form);
        return ApiResponse.of(CREATE_SUBFUNCTION_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_SUBFUNCTION')")
    @PutMapping("/{id}")
    public ApiResponse<Void> edit(@RequestBody SubFunctionForm form, @PathVariable String id) {
        subFunctionService.updateSubFunction(form, id);
        return ApiResponse.of(UPDATE_SUBFUNCTION_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_SUBFUNCTION')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        subFunctionService.deleteSubFunction(id);
        return ApiResponse.of(UPDATE_SUBFUNCTION_SUCCESS);
    }
}
