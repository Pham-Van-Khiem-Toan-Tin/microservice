package com.ecommerce.catalogservice.controller;


import static com.ecommerce.catalogservice.constants.Constants.*;

import com.ecommerce.catalogservice.dto.request.category.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.category.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.dto.response.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.CategoryDetailDTO;
import com.ecommerce.catalogservice.dto.response.CategoryOptionDTO;
import com.ecommerce.catalogservice.service.CategoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/categories")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PreAuthorize("hasAuthority('VIEW_CATEGORY_LIST')")
    @GetMapping
    public Page<CategoryDTO> findAllCategories(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<CategorySearchField> fields,
            @RequestParam(defaultValue = "name:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Sort sortObj = parseSort(sort, Sort.by("id").ascending());
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return categoryService.search(keyword, fields, pageable);
    }

    @PreAuthorize("hasAuthority('VIEW_CATEGORY_LIST')")
    @GetMapping("/options")
    public Set<CategoryOptionDTO> getParentCategoryOptions() {
        return categoryService.getParentCategories();
    }
    @PreAuthorize("hasAuthority('VIEW_CATEGORY_LIST')")
    @GetMapping("/leaf")
    public Set<CategoryOptionDTO> getCategoryOptions() {
        return categoryService.getCategoryLeafOptions();
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

    @PreAuthorize("hasAuthority('CREATE_CATEGORY')")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> createCategory(
            @RequestPart("data") CategoryCreateForm categoryForm,
            @RequestPart("image") MultipartFile image
    ) {
        categoryService.createCategory(categoryForm, image);
        return ApiResponse.ok(CREATE_CATEGORY_SUCCESS);
    }

    @PreAuthorize("hasAuthority('VIEW_CATEGORY')")
    @GetMapping("/{id}")
    public CategoryDetailDTO getCategoryById(@PathVariable String id) {
        return categoryService.getCategoryDetailDTO(id);
    }

    @PreAuthorize("hasAuthority('EDIT_CATEGORY')")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updateCategory(
            @RequestPart("data") CategoryUpdateForm categoryForm,
            @RequestPart(value = "image", required = false) MultipartFile image,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @PathVariable String id) throws JsonProcessingException {
        categoryService.updateCategory(categoryForm, image, idempotencyKey, id);
        return ApiResponse.ok(UPDATE_CATEGORY_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_CATEGORY')")
    @PatchMapping("/{id}")
    public ApiResponse<Void> changeStatus(@PathVariable String id) {
        categoryService.toggleActiveCategory(id);
        return ApiResponse.ok(UPDATE_CATEGORY_SUCCESS);
    }
    @PreAuthorize("hasAuthority('DELETE_CATEGORY')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok(DELETE_CATEGORY_SUCESS);
    }
}
