package com.ecommerce.productservice.service;

import com.ecommerce.productservice.constants.ResponseCode;
import com.ecommerce.productservice.dto.request.CategoryForm;
import com.ecommerce.productservice.dto.response.ApiResponse;
import com.ecommerce.productservice.entity.CategoryEntity;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface CategoryService {
    List<CategoryEntity> findAllParent(String id);
    CategoryEntity createCategory(CategoryForm form);
    CategoryEntity updateCategory();
    CategoryEntity deleteCategory();

}
