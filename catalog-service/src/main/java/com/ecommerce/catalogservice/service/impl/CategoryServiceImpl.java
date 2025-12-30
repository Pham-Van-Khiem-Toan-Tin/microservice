package com.ecommerce.catalogservice.service.impl;


import com.ecommerce.catalogservice.dto.request.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.*;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import com.ecommerce.catalogservice.entity.MenuSectionEntity;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.repository.MenuSectionRepository;
import com.ecommerce.catalogservice.service.CategoryService;

import com.ecommerce.catalogservice.service.CloudinaryService;
import com.ecommerce.catalogservice.utils.SlugUtils;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.ecommerce.catalogservice.constants.Constants.*;


@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Page<CategoryDTO> search(String keyword, List<CategorySearchField> fields, Pageable pageable) {
        Query query = new Query();
        Set<CategorySearchField> fs = (fields == null || fields.isEmpty()) ? EnumSet.of(CategorySearchField.NAME) : EnumSet.copyOf(fields);
        if (StringUtils.hasText(keyword)) {
            List<Criteria> ors = new ArrayList<>();
            for (CategorySearchField f : fs) {
                switch (f) {
                    case NAME -> ors.add(Criteria.where("name").regex(keyword, "i"));
                    case SLUG -> ors.add(Criteria.where("slug").regex(keyword, "i"));
                    case LEVEL -> {
                        Integer level = parseIntOrThrow(keyword);
                        ors.add(Criteria.where("level").is(level));
                    }
                }
            }
            query.addCriteria(new Criteria().orOperator(ors));
        }
        long total = mongoTemplate.count(query, CategoryEntity.class);
        query.with(pageable);
        List<CategoryEntity> categoryEntities = mongoTemplate.find(query, CategoryEntity.class);
        List<CategoryDTO> categoryDTOS = categoryEntities.stream().map(cat -> CategoryDTO
                .builder()
                .id(cat.getId())
                .name(cat.getName())
                .slug(cat.getSlug())
                .level(cat.getLevel())
                .isVisible(cat.getIsVisible())
                .build()).collect(Collectors.toList());
        return new PageImpl<>(categoryDTOS, pageable, total);
    }

    private Integer parseIntOrThrow(String s) {
        try {
            return Integer.valueOf(s.trim());
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public CategoryEntity createCategory(CategoryCreateForm categoryForm) {
        if (!StringUtils.hasText(categoryForm.getName())
                || !StringUtils.hasText(categoryForm.getIconUrl())
                || categoryForm.getImage() == null
                || categoryForm.getImage().isEmpty()
        ) throw new BusinessException(VALIDATE_FAIL);
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryForm.getName())
                .slug(SlugUtils.toSlug(categoryForm.getName()))
                .iconUrl(categoryForm.getIconUrl())
                .isVisible(categoryForm.getIsVisible())
                .sortOrder(categoryForm.getSortOrder())
                .menuLabel(categoryForm.getMenuLabel())

                .isFeatured(categoryForm.getIsFeatured())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        if (StringUtils.hasText(categoryForm.getParentId())) {
            CategoryEntity categoryParent = categoryRepository.findById(categoryForm.getParentId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            if (categoryParent.getLevel() == 2) throw new BusinessException(VALIDATE_FAIL);
            categoryEntity.setParentId(categoryParent.getId());
            categoryEntity.setLevel(categoryParent.getLevel() + 1);
            if (categoryEntity.getLevel() == 2) categoryEntity.setIsLeaf(true);
        } else {
            categoryEntity.setParentId(null);
            categoryEntity.setLevel(0);
        }
        CloudinaryUploadResult upload = cloudinaryService.updloadImage(categoryForm.getImage(), "categories");
        categoryEntity.setImageUrl(upload.getUrl());
        categoryEntity.setImagePublicId(upload.getPublicId());
        return categoryRepository.save(categoryEntity);
    }

    @Override
    public CategoryEntity updateCategory(CategoryUpdateForm categoryForm) {
        return null;
    }

    @Override
    public CategoryEntity deleteCategory(String id) {
        return null;
    }

    @Override
    public Set<CategoryOptionDTO> getParentCategories() {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("level").in(List.of(0, 1))
        );
        query.with(Sort.by(Sort.Direction.ASC, "level", "name"));
        query.fields()
                .include("_id")
                .include("name")
                .include("level")
                .include("parentId");
        List<CategoryEntity> categoryEntities = mongoTemplate.find(query, CategoryEntity.class);
        return categoryEntities.stream().map(ct ->
                        CategoryOptionDTO
                                .builder()
                                .id(ct.getId())
                                .name(ct.getName())
                                .level(ct.getLevel())
                                .parentId(ct.getParentId())
                                .build())
                .collect(Collectors.toSet());
    }

    @Override
    public CategoryDetailDTO getCategoryDetailDTO(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(VALIDATE_FAIL);
        CategoryEntity categoryEntity = categoryRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        CategoryEntity parent = null;
        if (categoryEntity.getParentId() != null)
            parent = categoryRepository.findById(categoryEntity.getParentId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
        return CategoryDetailDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .iconUrl(categoryEntity.getIconUrl())
                .isVisible(categoryEntity.getIsVisible())
                .sortOrder(categoryEntity.getSortOrder())
                .menuLabel(categoryEntity.getMenuLabel())
                .isFeatured(categoryEntity.getIsFeatured())
                .imageUrl(categoryEntity.getImageUrl())
                .parentName(parent != null ? parent.getName() : "Danh mục hiện tại là danh mục gốc")
                .slug(categoryEntity.getSlug())
                .build();
    }
}
