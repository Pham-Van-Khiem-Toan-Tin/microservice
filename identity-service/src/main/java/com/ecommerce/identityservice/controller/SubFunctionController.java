package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.request.SubFunctionCreateForm;
import com.ecommerce.identityservice.dto.request.SubFunctionEditForm;
import com.ecommerce.identityservice.dto.request.SubFunctionOptionForm;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.SubFunctionDTO;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.service.SubFunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static com.ecommerce.identityservice.constants.Constants.*;

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
            @RequestParam(defaultValue = "code:asc") String sort,
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
    public ApiResponse<UUID> add(@RequestBody SubFunctionCreateForm form) {
        SubFunctionEntity subFunctionEntity = subFunctionService.createSubFunction(form);
        return ApiResponse.of(CREATE_SUBFUNCTION_SUCCESS, subFunctionEntity.getId());
    }
    @PreAuthorize("hasAuthority('EDIT_SUBFUNCTION')")
    @PutMapping("/{id}")
    public ApiResponse<Void> edit(@RequestBody SubFunctionEditForm form, @PathVariable String id) {
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
