package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.CategoryForm;
import com.ecommerce.catalogservice.entity.CategoryEntity;

import java.util.List;

public interface CategoryService {
    CategoryEntity createCategory(CategoryForm categoryForm);
    CategoryEntity updateCategory(CategoryForm categoryForm);
    CategoryEntity deleteCategory(String id);

}
