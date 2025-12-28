package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.request.CategoryForm;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import com.ecommerce.catalogservice.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/category")
public class CategoryController {
    @Autowired
    private CategoryService categoryService;
//    @GetMapping("/parent/{id}")
//    public ResponseEntity<List<CategoryEntity>> findAllParent(@PathVariable("id") String id) {
//        List<CategoryEntity> categoryEntityList = categoryService.findAllParent(id);
//        return ResponseEntity.ok(categoryEntityList);
//    }
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse<Constants>> createCategory(@RequestBody CategoryForm form) {
//        categoryService.createCategory(form);
//        return ResponseEntity.ok(ApiResponse.of(Constants.CREATE_PRODUCT_SUCCESS));
//    }
//    @PutMapping("/edit/{id}")
//    public ResponseEntity<ApiResponse<Constants>> updateCategory(@PathVariable("id") String id) {
//        return ResponseEntity.ok(ApiResponse.of(Constants.UPDATE_PRODUCT_SUCCESS));
//    }
}
