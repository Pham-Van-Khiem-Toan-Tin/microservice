package com.ecommerce.catalogservice.service.impl;


import com.ecommerce.catalogservice.dto.request.attribute.AttributeConfigForm;
import com.ecommerce.catalogservice.dto.request.category.CategoryCreateForm;
import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.category.CategoryUpdateForm;
import com.ecommerce.catalogservice.dto.response.*;
import com.ecommerce.catalogservice.entity.*;
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
                || image.isEmpty()) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        String slug = SlugUtils.toSlug(categoryForm.getName());
        if (categoryRepository.existsBySlug(slug)) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        // 1) Validate + map attribute configs (đừng map thẳng)
        List<AttributeConfig> mappedConfigs = validateAndMapAttributeConfigs(categoryForm.getAttributeConfigs());

        CategoryEntity categoryEntity = CategoryEntity.builder()
                .name(categoryForm.getName())
                .slug(slug)
                .icon(categoryForm.getIcon())
                .active(categoryForm.isActive())
                .attributeConfigs(mappedConfigs)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .isLeaf(true) // category mới tạo luôn là leaf
                .build();

        // 2) Parent / Level / Ancestor
        if (StringUtils.hasText(categoryForm.getParentId())) {
            CategoryEntity parent = categoryRepository.findById(categoryForm.getParentId())
                    .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

            // giới hạn depth
            if (parent.getLevel() == 2) throw new BusinessException(VALIDATE_FAIL);

            categoryEntity.setParentId(parent.getId());
            categoryEntity.setLevel(parent.getLevel() + 1);

            // ancestor = parent.ancestor + parent
            List<Ancestors> ancestors = new ArrayList<>();
            if (parent.getAncestor() != null && !parent.getAncestor().isEmpty()) {
                ancestors.addAll(parent.getAncestor());
            }
            ancestors.add(new Ancestors(parent.getId(), parent.getName(), parent.getSlug()));
            categoryEntity.setAncestor(ancestors);

            // parent chắc chắn không còn leaf
            if (Boolean.TRUE.equals(parent.getIsLeaf())) {
                parent.setIsLeaf(false);
                categoryRepository.save(parent);
            }
        } else {
            categoryEntity.setParentId(null);
            categoryEntity.setLevel(0);
            categoryEntity.setAncestor(List.of()); // không để null
        }

        // 3) Upload image
        CloudinaryUploadResult upload = cloudinaryService.uploadImage(image, "categories");
        categoryEntity.setImage(new ImageEntity(upload.getUrl(), upload.getPublicId()));

        categoryRepository.save(categoryEntity);
    }

    private List<AttributeConfig> validateAndMapAttributeConfigs(List<AttributeConfigForm> forms) {
        if (forms == null) return List.of();

        // validate cơ bản + chống trùng code
        for (AttributeConfigForm f : forms) {
            if (f == null || !StringUtils.hasText(f.getCode())) throw new BusinessException(VALIDATE_FAIL);
            if (f.getIsRequired() == null) throw new BusinessException(VALIDATE_FAIL);
            if (f.getIsFilterable() == null) throw new BusinessException(VALIDATE_FAIL);
            if (f.getDisplayOrder() == null) throw new BusinessException(VALIDATE_FAIL);
            if (f.getDisplayOrder() < 0) throw new BusinessException(VALIDATE_FAIL);
        }

        List<String> codes = forms.stream()
                .map(AttributeConfigForm::getCode)
                .map(String::trim)
                .distinct()
                .toList();

        // query 1 lần
        List<AttributeEntity> attrs = attributeRepository.findByCodeIn(codes);
        var attrByCode = attrs.stream()
                .collect(java.util.stream.Collectors.toMap(AttributeEntity::getCode, a -> a, (a, b) -> a));

        // đảm bảo đủ attribute
        if (attrByCode.size() != codes.size()) throw new BusinessException(VALIDATE_FAIL);

        // validate từng config theo attribute
        for (AttributeConfigForm f : forms) {
            AttributeEntity attr = attrByCode.get(f.getCode().trim());
            if (attr == null) throw new BusinessException(VALIDATE_FAIL);

            // active/deleted check
            if (Boolean.TRUE.equals(attr.getDeleted())) throw new BusinessException(VALIDATE_FAIL);
            if (!Boolean.TRUE.equals(attr.getActive())) throw new BusinessException(VALIDATE_FAIL);

            AttributeDataType type = attr.getDataType();
            boolean isSelectType = type == AttributeDataType.SELECT || type == AttributeDataType.MULTI_SELECT;

            List<String> allowed = f.getAllowedOptionIds();
            boolean hasAllowed = allowed != null && !allowed.isEmpty();

            if (isSelectType) {
                if (!hasAllowed) throw new BusinessException(VALIDATE_FAIL);

                // option id hợp lệ + active + not deprecated
                var optionById = (attr.getOptions() == null ? List.<OptionEntity>of() : attr.getOptions()).stream()
                        .filter(o -> o != null && StringUtils.hasText(o.getId()))
                        .collect(java.util.stream.Collectors.toMap(OptionEntity::getId, o -> o, (a, b) -> a));

                for (String optId : allowed) {
                    if (!StringUtils.hasText(optId)) throw new BusinessException(VALIDATE_FAIL);

                    OptionEntity opt = optionById.get(optId);
                    if (opt == null) throw new BusinessException(VALIDATE_FAIL);
                    if (!Boolean.TRUE.equals(opt.getActive())) throw new BusinessException(VALIDATE_FAIL);
                    if (Boolean.TRUE.equals(opt.getDeprecated())) throw new BusinessException(VALIDATE_FAIL);
                }

                // chống trùng optionId
                long distinct = allowed.stream().filter(StringUtils::hasText).distinct().count();
                if (distinct != allowed.size()) throw new BusinessException(VALIDATE_FAIL);

            } else {
                // non-select => không được gửi allowedOptionIds
                if (hasAllowed) throw new BusinessException(VALIDATE_FAIL);
            }
        }

        // Map sang entity config (sau khi validate xong)
        return forms.stream().map(f -> new AttributeConfig(
                f.getCode().trim(),
                f.getIsRequired(),
                f.getIsFilterable(),
                f.getDisplayOrder(),
                (f.getAllowedOptionIds() == null ? null : f.getAllowedOptionIds()),
                true
        )).toList();
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
        category.setAttributeConfigs(categoryForm.getAttributeConfigs().stream().map(
                at -> new AttributeConfig(
                        at.getCode(),
                        at.getIsRequired(),
                        at.getIsFilterable(),
                        at.getDisplayOrder(),
                        at.getAllowedOptionIds(),
                        true)
        ).toList());
        String newParentId = categoryForm.getParentId();
        String oldParentId = category.getParentId();
        if (newParentId.equals(category.getId())) throw new BusinessException(VALIDATE_FAIL);
        boolean parentChanged = StringUtils.hasText(newParentId)
                && !Objects.equals(oldParentId, newParentId);
        if (!StringUtils.hasText(newParentId)) {
            category.setParentId(null);
            category.setLevel(0);
            category.setIsLeaf(false);
            // set root
        } else if (parentChanged) {
            CategoryEntity categoryParent = categoryRepository.findById(categoryForm.getParentId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            if (categoryParent.getAncestor() != null) {
                boolean parentIsDescendant = categoryParent.getAncestor().stream()
                        .anyMatch(a -> Objects.equals(a.getId(), category.getId()));
                if (parentIsDescendant) throw new BusinessException(VALIDATE_FAIL);
            }
            if (categoryParent.getLevel() == 2) throw new BusinessException(VALIDATE_FAIL);
            category.setParentId(categoryParent.getId());
            category.setLevel(categoryParent.getLevel() + 1);
            List<Ancestors> ancestors = new ArrayList<>();
            List<Ancestors> parentAnc = categoryParent.getAncestor();
            if (parentAnc != null) ancestors.addAll(parentAnc);

            ancestors.add(new Ancestors(
                    categoryParent.getId(),
                    categoryParent.getName(),
                    categoryParent.getSlug()
            ));

            category.setAncestor(ancestors);
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
                            AttributeConfig::getCode,
                            config -> config.getAllowedOptionIds() == null
                                    ? new ArrayList<>()
                                    : config.getAllowedOptionIds()
                    ));
            Map<String, Boolean> requiredMap = attributeConfigs.stream()
                    .collect(Collectors.toMap(
                            AttributeConfig::getCode,
                            AttributeConfig::isRequired // hoặc c.isRequired() tùy class
                    ));

            Map<String, Boolean> filterableMap = attributeConfigs.stream()
                    .collect(Collectors.toMap(
                            AttributeConfig::getCode,
                            AttributeConfig::isFilterable
                    ));

            Map<String, Integer> displayOrderMap = attributeConfigs.stream()
                    .collect(Collectors.toMap(
                            AttributeConfig::getCode,
                            AttributeConfig::getDisplayOrder
                    ));
            List<AttributeEntity> attributeEntities = attributeRepository
                    .findAllByCodeIn(allowedOptionsMap.keySet());
            attributeConfigDTOS = attributeEntities.stream()
                    .map(entity -> {
                        List<String> allowIds = allowedOptionsMap
                                .getOrDefault(entity.getCode(), Collections.emptyList());
                        Boolean isRequired = requiredMap.getOrDefault(entity.getCode(), false);
                        Boolean isFilterable = filterableMap.getOrDefault(entity.getCode(), false);
                        Integer displayOrder = displayOrderMap.getOrDefault(entity.getCode(), 0);
                        return AttributeConfigDTO
                                .builder()
                                .id(entity.getId())
                                .code(entity.getCode())
                                .label(entity.getLabel())
                                .unit(entity.getUnit())
                                .isRequired(isRequired)
                                .isFilterable(isFilterable)
                                .displayOrder(displayOrder)
                                .dataType(entity.getDataType())
                                .optionsValue(entity.getOptions() == null
                                        ? Collections.emptyList()
                                        : entity.getOptions().stream()
                                        .map(opt -> AttributeOptionDTO
                                                .builder()
                                                .id(opt.getId())
                                                .deprecated(opt.getDeprecated())
                                                .selected(allowIds.contains(opt.getId()))
                                                .active(opt.getActive())
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
