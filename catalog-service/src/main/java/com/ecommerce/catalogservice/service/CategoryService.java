package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.CategoryDetailDTO;
import com.ecommerce.catalogservice.dto.response.CategoryOptionDTO;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    Page<CategoryDTO> search(String keyword, List<CategorySearchField> fields, Pageable pageable);
//    CategoryEntity createCategory(CategoryCreateForm categoryForm);
//    CategoryEntity updateCategory(CategoryUpdateForm categoryForm, String id);
//    void deleteCategory(String id);
//    Set<CategoryOptionDTO> getParentCategories();
//    CategoryDetailDTO getCategoryDetailDTO(String id);
}
