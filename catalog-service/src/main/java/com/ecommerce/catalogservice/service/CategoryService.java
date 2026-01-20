package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.category.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.category.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.category.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.category.CategoryDetailDTO;
import com.ecommerce.catalogservice.dto.response.CategoryOptionDTO;
import com.ecommerce.catalogservice.dto.response.category.FilterDTO;
import com.ecommerce.catalogservice.dto.response.menu.MenuDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;

public interface CategoryService {
    Page<CategoryDTO> search(String keyword, List<CategorySearchField> fields, Pageable pageable);
    void createCategory(CategoryCreateForm categoryForm, MultipartFile image);
    void updateCategory(CategoryUpdateForm categoryForm, MultipartFile image, String idemKey, String id) throws JsonProcessingException;
    void deleteCategory(String id);
    Set<CategoryOptionDTO> getCategoryLeafOptions();
    Set<CategoryOptionDTO> getParentCategories();
    CategoryDetailDTO getCategoryDetailDTO(String id);
    void toggleActiveCategory(String id);
    List<MenuDTO> getMenus();
    FilterDTO getFilterDTO(String id);
}
