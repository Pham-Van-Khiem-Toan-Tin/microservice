package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.CategoryDetailDTO;
import com.ecommerce.catalogservice.dto.response.CategoryOptionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    Page<CategoryDTO> search(String keyword, List<CategorySearchField> fields, Pageable pageable);
    void createCategory(CategoryCreateForm categoryForm, MultipartFile image);
    void updateCategory(CategoryUpdateForm categoryForm, MultipartFile image, String id);
    void deleteCategory(String id);
    Set<CategoryOptionDTO> getCategoryLeafOptions();
    Set<CategoryOptionDTO> getParentCategories();
    CategoryDetailDTO getCategoryDetailDTO(String id);
}
