package com.ecommerce.catalogservice.service.impl;

import static com.ecommerce.catalogservice.constants.Constants.*;

import com.ecommerce.catalogservice.dto.request.brand.BrandCreateForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandEditForm;
import com.ecommerce.catalogservice.dto.request.brand.BrandSearchField;
import com.ecommerce.catalogservice.dto.response.*;
import com.ecommerce.catalogservice.entity.BrandEntity;
import com.ecommerce.catalogservice.entity.BrandStatus;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.repository.BrandRepository;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.service.BrandService;
import com.ecommerce.catalogservice.service.CloudinaryService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
import com.ecommerce.catalogservice.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BrandServiceImpl implements BrandService {
    @Autowired
    BrandRepository brandRepository;
    @Autowired
    CloudinaryService cloudinaryService;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    CategoryRepository categoryRepository;

    @Override
    public Page<BrandDTOS> searchBrands(String keyword, List<BrandSearchField> fields, Pageable pageable) {
        Query query = new Query();
        Set<BrandSearchField> fs = (fields == null || fields.isEmpty())
                ? EnumSet.of(BrandSearchField.name)
                : EnumSet.copyOf(fields);
        if (StringUtils.hasText(keyword)) {
            List<Criteria> ors = new ArrayList<>();
            for (BrandSearchField f : fs) {
                switch (f) {
                    case name -> ors.add(Criteria.where("name").regex(keyword, "i"));
                    case slug -> ors.add(Criteria.where("slug").regex(keyword, "i"));
                    case status -> ors.add(Criteria.where("status").regex(keyword, "i"));
                }
            }
            query.addCriteria(new Criteria().orOperator(ors));
        }
        long total = mongoTemplate.count(query, BrandEntity.class);
        query.with(pageable);
        List<BrandEntity> brands = mongoTemplate.find(query, BrandEntity.class);
        Set<String> categoriesIds = brands.stream().flatMap(b -> b.getCategories().stream())
                .collect(Collectors.toSet());
        Map<String, String> categoryMap = categoryRepository.findAllById(categoriesIds)
                .stream().collect(Collectors.toMap(CategoryEntity::getId, CategoryEntity::getName));
        List<BrandDTOS> brandDTOS = brands.stream()
                .map(br -> BrandDTOS.builder()
                        .id(br.getId())
                        .name(br.getName())
                        .status(br.getStatus())
                        .categories(br.getCategories().stream().map(categoryMap::get)
                                .filter(Objects::nonNull).collect(Collectors.toList()))
                        .build())
                .toList();
        return new PageImpl<>(brandDTOS, pageable, total);
    }

    @Override
    public BrandDetailDTO getBrand(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        BrandEntity brand = brandRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        List<String> categoriesIds = brand.getCategories();
        List<CategoryEntity> categories = categoryRepository.findAllById(categoriesIds);
        String dateFormat = "dd/MM/yyyy";
        return BrandDetailDTO.builder()
                .id(brand.getId())
                .name(brand.getName())
                .slug(brand.getSlug())
                .description(brand.getDescription())
                .status(brand.getStatus())
                .logo(brand.getLogo())
                .createdDate(DateTimeUtils.instantToString(brand.getCreatedAt(), dateFormat))
                .updatedDate(DateTimeUtils.instantToString(brand.getUpdatedAt(), dateFormat))
                .categories(categories.stream().map(
                        ct -> CategoryOptionDTO.builder()
                                .id(ct.getId())
                                .name(ct.getName())
                                .build()
                ).toList())
                .build();
    }

    @Override
    public void addBrand(BrandCreateForm form, MultipartFile image) {
        if (!StringUtils.hasText(form.getName())
                || !StringUtils.hasText(form.getSlug())
                || form.getCategories() == null
                || form.getCategories().isEmpty()
                || image == null
                || image.isEmpty())
            throw new BusinessException(VALIDATE_FAIL);
        if (brandRepository.existsByNameOrSlug(form.getName(), form.getSlug()))
            throw new BusinessException(VALIDATE_FAIL);
        BrandEntity brand = BrandEntity.builder()
                .name(form.getName())
                .slug(form.getSlug())
                .description(form.getDescription())
                .categories(form.getCategories())
                .status(form.getStatus())
                .build();
        CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(image, "brands");
        ImageEntity logo = new ImageEntity(uploadResult.getUrl(), uploadResult.getPublicId());
        brand.setLogo(logo);
        brand.setCreatedAt(Instant.now());
        brand.setUpdatedAt(Instant.now());
        brandRepository.save(brand);
    }

    @Override
    public void updateBrand(BrandEditForm form, MultipartFile image, String id) {
        if (!StringUtils.hasText(form.getName())
                || !StringUtils.hasText(form.getSlug())
                || form.getCategories() == null
                || form.getCategories().isEmpty()
                || !id.equals(form.getId()))
            throw new BusinessException(VALIDATE_FAIL);
        BrandEntity brand = brandRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        if (!brand.getName().equals(form.getName()) && brandRepository.existsByName(form.getName()))
            throw new BusinessException(VALIDATE_FAIL);
        if (!brand.getSlug().equals(form.getSlug()) && brandRepository.existsBySlug(form.getSlug()))
            throw new BusinessException(VALIDATE_FAIL);
        Instant now = Instant.now();
        brand.setName(form.getName());
        brand.setSlug(form.getSlug());
        brand.setDescription(form.getDescription());
        brand.setCategories(form.getCategories());
        brand.setStatus(form.getStatus());
        if (image != null && !image.isEmpty()) {
            cloudinaryService.deleteImage(brand.getLogo().getImagePublicId(), "brands");
            CloudinaryUploadResult uploadResult = cloudinaryService.uploadImage(image, "brands");
            ImageEntity logo = new ImageEntity(uploadResult.getUrl(), uploadResult.getPublicId());
            brand.setLogo(logo);
        }
        brand.setUpdatedBy(AuthenticationUtils.getUserId());
        brand.setUpdatedAt(now);
        brandRepository.save(brand);
    }

    @Override
    public void deleteBrand(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        BrandEntity brand = brandRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        brandRepository.delete(brand);
    }

    @Override
    public List<BrandOptionDTOS> getBrandOptions() {
        List<BrandEntity> brands = brandRepository.findAllByStatus(BrandStatus.active);

        return brands.stream().map(
                br -> BrandOptionDTOS.builder()
                        .id(br.getId())
                        .name(br.getName())
                        .build()
        ).toList();
    }

    @Override
    public void toggleActiveBrand(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        BrandEntity brand = brandRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        BrandStatus brandStatus = BrandStatus.active;
        if (brand.getStatus() == BrandStatus.active) {
            brandStatus = BrandStatus.hidden;
        }
        brand.setStatus(brandStatus);
        brandRepository.save(brand);
    }
}
