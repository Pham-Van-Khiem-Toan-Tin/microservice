package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.dto.request.attribute.*;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.dto.response.attribute.AttributeDTO;
import com.ecommerce.catalogservice.dto.response.attribute.AttributeDetailDTO;
import com.ecommerce.catalogservice.service.AttributeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ecommerce.catalogservice.constants.Constants.*;

@RestController
@RequestMapping("/attributes")
public class AttributeController {
    @Autowired
    private AttributeService attributeService;
    @PreAuthorize("hasAuthority('VIEW_ATTRIBUTE_LIST')")
    @GetMapping
    public Page<AttributeDTO> searchAttribute(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<AttributeSearchField> fields,
            @RequestParam(defaultValue = "label:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sortObj = parseSort(sort, Sort.by("code").ascending());
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return attributeService.search(keyword, fields, pageable);
    }
    @PreAuthorize("hasAuthority('VIEW_ATTRIBUTE_LIST')")
    @PostMapping("/options")
    public List<AttributeDetailDTO> searchAttributeOption(@RequestBody AttributeOptionForm form) {
        return attributeService.searchAttributeOption(form);
    }
    @PreAuthorize("hasAuthority('EDIT_ATTRIBUTE')")
    @PatchMapping("/revoke/{id}")
    public ApiResponse<Void> revokeAttribute(@PathVariable String id) {
        attributeService.revokeAttribute(id);
        return ApiResponse.ok(ATTRIBUTE_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_ATTRIBUTE')")
    @PatchMapping("/revoke-option/{id}")
    public ApiResponse<Void> revokeAttributeOption(@PathVariable String id, @RequestBody RevokeOptionForm form) {
        attributeService.revokeAttributeOption(id, form);
        return ApiResponse.ok(ATTRIBUTE_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_ATTRIBUTE')")
    @PatchMapping("/{id}")
    public ApiResponse<Void> toggleActiveAttribute(@PathVariable String id) {
        attributeService.changeActiveAttribute(id);
        return ApiResponse.ok(ATTRIBUTE_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('VIEW_ATTRIBUTE')")
    @GetMapping("/{id}")
    public AttributeDetailDTO getAttribute(@PathVariable String id) {
        return attributeService.getAttributeDetail(id);
    }
    @PreAuthorize("hasAuthority('CREATE_ATTRIBUTE')")
    @PostMapping
    public ApiResponse<Void> create(@RequestBody AttributeCreateForm form) {
        attributeService.addAttribute(form);
        return ApiResponse.ok(ATTRIBUTE_CREATE_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_ATTRIBUTE')")
    @PutMapping("/{id}")
    public ApiResponse<Void> edit(@RequestBody AttributeEditForm form, @PathVariable String id) {
        attributeService.updateAttribute(form, id);
        return ApiResponse.ok(ATTRIBUTE_EDIT_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_ATTRIBUTE')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        attributeService.deleteAttribute(id);
        return ApiResponse.ok(ATTRIBUTE_DELETE_SUCCESS);
    }
    private Sort parseSort(String raw, Sort fallback) {
        try {
            if (raw == null || raw.isBlank()) return fallback;
            String[] parts = raw.split(":");
            String field = parts[0];
            Sort.Direction dir =
                    (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
            return Sort.by(dir, field);
        } catch (Exception e) {
            return fallback;
        }
    }
}
