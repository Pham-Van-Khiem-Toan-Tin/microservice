package com.ecommerce.categoryservice.service.impl;

import com.ecommerce.categoryservice.DTO.CategoryDTO;
import com.ecommerce.categoryservice.entity.Category;
import com.ecommerce.categoryservice.repository.CategoryRepository;
import com.ecommerce.categoryservice.service.CategoryService;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;

    @Transactional
    public List<CategoryDTO> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        Map<Long, CategoryDTO> categoryMap = new HashMap<>();
        categories.stream().forEach(category -> {
            categoryMap.put(category.getId(), new CategoryDTO(category.getId(), category.getName(), new ArrayList<>()));
        });
        categories.stream().forEach(category -> {
            if (category.getParent() != null) {
                CategoryDTO parentDTO = categoryMap.get(category.getParent().getId());
                if (parentDTO != null) {
                    parentDTO.getChildren().add(categoryMap.get(category.getId()));
                }
            }
        });
        // Lấy ra danh sách các danh mục cha (không có parent)
        return categoryMap.values().stream()
                .filter(categoryDTO -> categoryDTO.getChildren().size() > 0)
                .collect(Collectors.toList());
    }

    public Optional<Category> getCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    public Category saveCategory(Category category) {
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }

    public void addSubCategory(Long parentId, Category childCategory) {
        Category parentCategory = categoryRepository.findById(parentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục cha với id: " + parentId));
        parentCategory.addChildCategory(childCategory);
        categoryRepository.save(parentCategory);
    }
}
