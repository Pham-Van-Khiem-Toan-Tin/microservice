package com.ecommerce.catalogservice.controller;


import static com.ecommerce.catalogservice.constants.Constants.*;
import com.ecommerce.catalogservice.dto.request.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.dto.response.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.CategoryDetailDTO;
import com.ecommerce.catalogservice.dto.response.CategoryOptionDTO;
import com.ecommerce.catalogservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
//    @PreAuthorize("hasAuthority('VIEW_CATEGORY_LIST')")
//    @GetMapping("/options")
//    public Set<CategoryOptionDTO> getParentCategoryOptions() {
//        return categoryService.getParentCategories();
//    }
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
//    @PreAuthorize("hasAuthority('CREATE_CATEGORY')")
//    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponse<Void> createCategory(@ModelAttribute CategoryCreateForm categoryForm) {
//        categoryService.createCategory(categoryForm);
//        return ApiResponse.ok(CREATE_CATEGORY_SUCCESS);
//    }
//    @PreAuthorize("hasAuthority('VIEW_CATEGORY')")
//    @GetMapping("/{id}")
//    public CategoryDetailDTO getCategoryById(@PathVariable String id) {
//        return categoryService.getCategoryDetailDTO(id);
//    }
//    @PreAuthorize("hasAuthority('EDIT_CATEGORY')")
//    @PutMapping( value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ApiResponse<Void> updateCategory(@ModelAttribute CategoryUpdateForm categoryForm, @PathVariable String id) {
//        categoryService.updateCategory(categoryForm, id);
//        return ApiResponse.ok(UPDATE_CATEGORY_SUCCESS);
//    }
//    @PreAuthorize("hasAuthority('DELETE_CATEGORY')")
//    @DeleteMapping("/{id}")
//    public ApiResponse<Void> deleteCategory(@PathVariable String id) {
//        categoryService.deleteCategory(id);
//        return ApiResponse.ok(DELETE_CATEGORY_SUCESS);
//    }
}
