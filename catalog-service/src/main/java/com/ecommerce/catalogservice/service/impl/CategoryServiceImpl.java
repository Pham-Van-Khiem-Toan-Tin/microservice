package com.ecommerce.catalogservice.service.impl;


import com.ecommerce.catalogservice.dto.request.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.*;
import com.ecommerce.catalogservice.entity.AttributeConfig;
import com.ecommerce.catalogservice.entity.AttributeEntity;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.repository.AttributeRepository;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.service.CategoryService;

import com.ecommerce.catalogservice.service.CloudinaryService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
import com.ecommerce.catalogservice.utils.SlugUtils;
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
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

import static com.ecommerce.catalogservice.constants.Constants.*;


@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private CloudinaryService cloudinaryService;

    @Override
    public Page<CategoryDTO> search(String keyword, List<CategorySearchField> fields, Pageable pageable) {
        Query query = new Query();
        Set<CategorySearchField> fs = (fields == null || fields.isEmpty())
                ? EnumSet.of(CategorySearchField.NAME) : EnumSet.copyOf(fields);
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
                .active(cat.isActive())
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
    public void createCategory(CategoryCreateForm categoryForm, MultipartFile image) {
        if (!StringUtils.hasText(categoryForm.getName())
                || !StringUtils.hasText(categoryForm.getIcon())
                || image == null
                || image.isEmpty()
        ) throw new BusinessException(VALIDATE_FAIL);
        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryForm.getName())
                .slug(SlugUtils.toSlug(categoryForm.getName()))
                .icon(categoryForm.getIcon())
                .active(categoryForm.isActive())
                .attributeConfigs(categoryForm.getAttributeConfigs().stream()
                        .map(at -> new AttributeConfig(
                                at.getId(),
                                at.isRequired(),
                                at.isFilterable(),
                                at.getDisplayOrder(),
                                at.getAllowedOptionIds()
                        ))
                        .toList())
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
        CloudinaryUploadResult upload = cloudinaryService.uploadImage(image, "categories");
        ImageEntity imageData = new ImageEntity(upload.getUrl(), upload.getPublicId());
        categoryEntity.setImage(imageData);
        categoryRepository.save(categoryEntity);
    }


    @Override
    public void updateCategory(CategoryUpdateForm categoryForm, MultipartFile image, String id) {
        if (!StringUtils.hasText(categoryForm.getName())
                || !StringUtils.hasText(categoryForm.getIcon())
                || !StringUtils.hasText(categoryForm.getName())
                || !StringUtils.hasText(categoryForm.getSlug())
                || !StringUtils.hasText(categoryForm.getId())
                || !id.equals(categoryForm.getId())
        ) throw new BusinessException(VALIDATE_FAIL);
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        if (!category.getSlug().equals(categoryForm.getSlug()) && categoryRepository.existsBySlug(categoryForm.getSlug()))
            throw new BusinessException(VALIDATE_FAIL);
        category.setName(categoryForm.getName());
        category.setIcon(categoryForm.getIcon());
        category.setSlug(categoryForm.getSlug());
        category.setActive(categoryForm.isActive());
        if (!StringUtils.hasText(categoryForm.getParentId())) {
            category.setParentId(null);
            category.setLevel(0);
            category.setIsLeaf(false);
        } else if (StringUtils.hasText(categoryForm.getParentId()) || !category.getParentId().equals(categoryForm.getParentId())) {
            CategoryEntity categoryParent = categoryRepository.findById(categoryForm.getParentId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            if (categoryParent.getLevel() == 2) throw new BusinessException(VALIDATE_FAIL);
            category.setParentId(categoryParent.getId());
            category.setLevel(categoryParent.getLevel() + 1);
            if (category.getLevel() == 2) category.setIsLeaf(true);
        }
        if (image != null && !image.isEmpty()) {
            cloudinaryService.deleteImage(category.getImage().getImagePublicId(), "categories");
            CloudinaryUploadResult upload = cloudinaryService.uploadImage(image, "categories");
            ImageEntity imageData = new ImageEntity(upload.getUrl(), upload.getPublicId());
            category.setImage(imageData);
        }
        category.setUpdatedBy(AuthenticationUtils.getUserId());
        category.setUpdatedAt(Instant.now());
        categoryRepository.save(category);
    }

    @Override
    public void deleteCategory(String id) {
        CategoryEntity category = categoryRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        cloudinaryService.deleteImage(category.getImage().getImagePublicId(), "categories");
        categoryRepository.delete(category);
    }

    @Override
    public Set<CategoryOptionDTO> getCategoryLeafOptions() {
        List<CategoryEntity> categoryEntities = categoryRepository.findAllByIsLeaf(true);
        return categoryEntities.stream().map(
                ct -> CategoryOptionDTO.builder()
                        .id(ct.getId())
                        .name(ct.getName())
                        .build()
        ).collect(Collectors.toSet());
    }

    @Override
    public Set<CategoryOptionDTO> getParentCategories() {
        Query query = new Query();
        query.addCriteria(
                Criteria.where("level").in(List.of(0, 1))
        );
        query.addCriteria(Criteria.where("active").is(true));
        query.with(Sort.by(Sort.Direction.ASC, "level", "name"));
        query.fields()
                .include("_id", "name", "parentId", "level");
        List<CategoryEntity> categoryEntities = mongoTemplate.find(query, CategoryEntity.class);
        if (categoryEntities.isEmpty()) return Collections.emptySet();


        return categoryEntities.stream().map(
                        ct -> CategoryOptionDTO
                                .builder()
                                .id(ct.getId())
                                .parentId(ct.getParentId())
                                .level(ct.getLevel())
                                .name(ct.getName())
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
        List<AttributeConfigDTO> attributeConfigDTOS = new ArrayList<>();
        List<AttributeConfig> attributeConfigs = categoryEntity.getAttributeConfigs();
        if (attributeConfigs != null && !attributeConfigs.isEmpty()) {
            Map<String, List<String>> allowedOptionsMap = attributeConfigs.stream()
                    .collect(Collectors.toMap(
                            AttributeConfig::getId,
                            config -> config.getAllowedOptionIds() == null
                                    ? new ArrayList<>()
                                    : config.getAllowedOptionIds()
                    ));
            List<AttributeEntity> attributeEntities = attributeRepository
                    .findAllById(allowedOptionsMap.keySet());
            attributeConfigDTOS = attributeEntities.stream()
                    .map(entity -> {
                        List<String> allowIds = allowedOptionsMap
                                .getOrDefault(entity.getId(), Collections.emptyList());
                        return AttributeConfigDTO
                                .builder()
                                .id(entity.getId())
                                .code(entity.getCode())
                                .label(entity.getLabel())
                                .unit(entity.getUnit())
                                .dataType(entity.getDataType())
                                .optionsValue(entity.getOptions() == null
                                        ? Collections.emptyList()
                                        : entity.getOptions().stream()
                                        .map(opt -> AttributeOptionDTO
                                                .builder()
                                                .active(allowIds.contains(opt.getValue()))
                                                .value(opt.getValue())
                                                .label(opt.getLabel())
                                                .build())
                                        .toList()
                                )
                                .build();
                    })
                    .toList();
        }
        return CategoryDetailDTO.builder()
                .id(categoryEntity.getId())
                .name(categoryEntity.getName())
                .slug(categoryEntity.getSlug())
                .icon(categoryEntity.getIcon())
                .active(categoryEntity.isActive())
                .image(categoryEntity.getImage())
                .parentName(parent != null ? parent.getName() : "Danh mục hiện tại là danh mục gốc")
                .parentId(parent != null ? parent.getId() : null)
                .attributeConfigs(attributeConfigDTOS)
                .build();
    }
}
