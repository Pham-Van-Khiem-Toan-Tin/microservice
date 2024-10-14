package com.ecommerce.categoryservice.controller;

import com.ecommerce.categoryservice.DTO.CategoryDTO;
import com.ecommerce.categoryservice.entity.Category;
import com.ecommerce.categoryservice.service.impl.CategoryServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/category")
public class CategoryController {
    @Autowired
    CategoryServiceImpl categoryService;

    @GetMapping("/all")
    @PreAuthorize("hasAuthority('CATEGORY_LIST')")
    public List<CategoryDTO> getAllCategories() {
        return categoryService.getAllCategories();
    }
//    @PostMapping("/add")
//    @PreAuthorize("hasAuthority('CATEGORY_ADD')")
//    public ResponseEntity<String> addCategory() {
//        categoryService.addSubCategory();
//    }
    @DeleteMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('CATEGORY_DELETE')")
    public ResponseEntity<String> deleteCategory(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ResponseEntity.ok("delete success");
    }
}
