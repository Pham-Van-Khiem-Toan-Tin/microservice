package com.ecommerce.productservice.controller;

import com.ecommerce.productservice.constants.ResponseCode;
import com.ecommerce.productservice.dto.request.CategoryForm;
import com.ecommerce.productservice.dto.response.ApiResponse;
import com.ecommerce.productservice.entity.CategoryEntity;
import com.ecommerce.productservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
    @GetMapping("/parent/{id}")
    public ResponseEntity<List<CategoryEntity>> findAllParent(@PathVariable("id") String id) {
        List<CategoryEntity> categoryEntityList = categoryService.findAllParent(id);
        return ResponseEntity.ok(categoryEntityList);
    }
    @PostMapping("/create")
    public ResponseEntity<ApiResponse<ResponseCode>> createCategory(@RequestBody CategoryForm form) {
        categoryService.createCategory(form);
        return ResponseEntity.ok(ApiResponse.of(ResponseCode.CREATE_PRODUCT_SUCCESS));
    }
    @PutMapping("/edit/{id}")
    public ResponseEntity<ApiResponse<ResponseCode>> updateCategory(@PathVariable("id") String id) {
        return ResponseEntity.ok(ApiResponse.of(ResponseCode.UPDATE_PRODUCT_SUCCESS));
    }
}
