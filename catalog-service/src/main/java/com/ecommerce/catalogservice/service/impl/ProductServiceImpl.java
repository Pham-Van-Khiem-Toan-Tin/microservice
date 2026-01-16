package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.product.*;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.dto.response.CategoryDTO;
import com.ecommerce.catalogservice.dto.response.CloudinaryUploadResult;
import com.ecommerce.catalogservice.dto.response.product.*;
import com.ecommerce.catalogservice.dto.response.sku.SkuDetailDTO;
import com.ecommerce.catalogservice.entity.*;
import com.ecommerce.catalogservice.repository.*;
import com.ecommerce.catalogservice.service.CloudinaryService;
import com.ecommerce.catalogservice.service.ProductService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.ecommerce.catalogservice.constants.Constants.*;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private BrandRepository brandRepository;
    @Autowired
    private AttributeRepository attributeRepository;
    @Autowired
    private CloudinaryService cloudinaryService;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private SkuRepository skuRepository;
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    MongoTemplate mongoTemplate;
    @Autowired
    KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    RestTemplate restTemplate;

    @Override
    public Page<ProductDTO> search(String keyword, List<ProductSearchField> fields, Pageable pageable) {
        Query query = new Query();
        Set<ProductSearchField> fs = (fields == null || fields.isEmpty())
                ? EnumSet.of(ProductSearchField.name) : EnumSet.copyOf(fields);
        if (StringUtils.hasText(keyword)) {
            List<Criteria> ors = new ArrayList<>();
            for (ProductSearchField f : fs) {
                switch (f) {
                    case name -> ors.add(Criteria.where("name").regex(keyword, "i"));
                    case slug -> ors.add(Criteria.where("slug").regex(keyword, "i"));

                }
            }
            query.addCriteria(new Criteria().orOperator(ors));
        }
        long total = mongoTemplate.count(query, ProductEntity.class);
        query.with(pageable);
        List<ProductEntity> productEntities = mongoTemplate
                .find(query, ProductEntity.class);
        List<String> categoryIds = productEntities.stream()
                .map(ProductEntity::getCategoryId)
                .distinct().toList();
        List<String> brandIds = productEntities
                .stream()
                .map(ProductEntity::getBrandId)
                .distinct().toList();
        Map<String, CategoryEntity> categoryMap = categoryRepository.findAllById(categoryIds)
                .stream()
                .collect(Collectors.toMap(CategoryEntity::getId, c -> c));
        Map<String, BrandEntity> brandMap = brandRepository.findAllById(brandIds)
                .stream()
                .collect(Collectors.toMap(BrandEntity::getId, b -> b));
        List<ProductDTO> dtos = productEntities.stream()
                .map(pr -> {
                    CategoryEntity c = categoryMap.get(pr.getCategoryId());
                    BrandEntity b = brandMap.get(pr.getBrandId());
                    return ProductDTO.builder()
                            .id(pr.getId())
                            .name(pr.getName())
                            .slug(pr.getSlug())
                            .category(Category.builder()
                                    .id(c.getId())
                                    .name(c.getName())
                                    .build())
                            .brand(Brand.builder()
                                    .id(b.getId())
                                    .name(b.getName())
                                    .build())
                            .status(pr.getStatus())
                            .build();
                }).toList();

        return new PageImpl<>(dtos, pageable, total);
    }

    @Override
    public ProductDetailDTO productDetailDTO(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        ProductEntity productEntity = productRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        CategoryEntity category = categoryRepository.findById(productEntity.getCategoryId()).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        BrandEntity brand = brandRepository.findById(productEntity.getBrandId()).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        List<String> specAttributeIds = productEntity.getSpecs()
                .stream()
                .map(ProductSpecs::getId)
                .toList();
        Map<String, List<OptionEntity>> specs = attributeRepository.findAllByIdIn(specAttributeIds)
                .stream()
                .collect(Collectors.toMap(AttributeEntity::getId, AttributeEntity::getOptions));
        List<SkuEntity> skus = skuRepository.findAllBySpuId(productEntity.getId());
        return ProductDetailDTO.builder()
                .id(productEntity.getId())
                .name(productEntity.getName())
                .slug(productEntity.getSlug())
                .category(Category.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .build())
                .brand(Brand.builder()
                        .id(brand.getId())
                        .name(brand.getName())
                        .build())
                .numOfReviews(productEntity.getNumberOfReviews())
                .avgRating(productEntity.getAverageRating())
                .hasVariants(productEntity.getHasVariant())
                .warrantyMonth(productEntity.getWarrantyMonth())
                .description(productEntity.getDescription())
                .shortDescription(productEntity.getShortDescription())
                .thumbnail(productEntity.getThumbnail())

                .gallery(productEntity.getGallery())
                .specs(productEntity.getSpecs().stream()
                        .map(sp -> {

                                    Map<String, String> optionMultiple = specs.get(sp.getId())
                                            .stream()
                                            .collect(Collectors
                                                    .toMap(OptionEntity::getId, OptionEntity::getLabel));
                                    return ProductSpecDTO.builder()
                                            .id(sp.getId())
                                            .code(sp.getCode())
                                            .displayOrder(sp.getDisplayOrder())
                                            .dataType(sp.getDataType())
                                            .unit(sp.getUnit())
                                            .value(sp.getValue())
                                            .valueSelect(specs.get(sp.getId()) != null ? specs.get(sp.getId()).stream()
                                                    .filter(
                                                            vs -> vs.getId().equals(sp.getValueId())

                                                    ).map(i -> SpecOptionDTO.builder()
                                                            .id(i.getId())
                                                            .label(i.getLabel())
                                                            .build()).findFirst().orElse(null) : null)
                                            .valueMultiSelect(sp.getValueIds() != null ? sp.getValueIds().stream()
                                                    .map(v -> SpecOptionDTO.builder()
                                                            .id(v)
                                                            .label(optionMultiple.get(v))
                                                            .build())
                                                    .toList()
                                                    : null)
                                            .label(sp.getLabel())
                                            .build();

                                }

                        )
                        .toList()
                )
                .status(productEntity.getStatus())
                .variantGroups(productEntity.getVariantGroups())
                .maxPrice(productEntity.getMaxPrice())
                .minPrice(productEntity.getMinPrice())
                .skus(skus.stream().map(
                        sk -> SkuDetailDTO.builder()
                                .id(sk.getId())
                                .name(sk.getName())
                                .price(sk.getPrice())
                                .spuId(sk.getSpuId())
                                .skuCode(sk.getSkuCode())
                                .selections(sk.getSelections())
                                .originalPrice(sk.getOriginalPrice())
                                .costPrice(sk.getCostPrice())
                                .thumbnail(sk.getThumbnail())
                                .active(sk.getActive())
                                .discontinued(sk.getDiscontinued())
                                .discontinuedReason(sk.getDiscontinuedReason())
                                .stock(sk.getStock())
                                .build()
                ).toList())
                .build();
    }

    @Transactional
    @Override
    public void addProduct(ProductCreateForm form, String idemKey) throws JsonProcessingException {
        if (!StringUtils.hasText(form.getName())
                || !StringUtils.hasText(form.getDescription())
                || !StringUtils.hasText(form.getSlug())
                || !StringUtils.hasText(form.getShortDescription())
                || !StringUtils.hasText(form.getBrandId())
                || !StringUtils.hasText(form.getCategoryId())
                || form.getSpecs() == null || form.getSpecs().isEmpty()
                || form.getThumbnail() == null || form.getThumbnail().isEmpty()
                || form.getGallery() == null || form.getGallery().isEmpty()
        ) throw new BusinessException(VALIDATE_FAIL);
        String thumbnailId = null;
        List<String> galleryIds = null;
        List<String> skuIds = new ArrayList<>();
        try {
            CloudinaryUploadResult thumbnailResult = cloudinaryService.uploadImage(form.getThumbnail(), "products");
            thumbnailId = thumbnailResult.getPublicId();
            List<ImageEntity> galleryImages = new ArrayList<>();
            for (MultipartFile file : form.getGallery()) {
                CloudinaryUploadResult galleryResult = cloudinaryService.uploadImage(file, "products");
                ImageEntity galleryItem = new ImageEntity(galleryResult.getUrl(), galleryResult.getPublicId());
                galleryImages.add(galleryItem);
            }
            galleryIds = galleryImages.stream().map(ImageEntity::getImagePublicId).toList();
            ImageEntity thumbnail = new ImageEntity(thumbnailResult.getUrl(), thumbnailResult.getPublicId());
            CategoryEntity categoryEntity = categoryRepository.findById(form.getCategoryId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            BrandEntity brandEntity = brandRepository.findById(form.getBrandId()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            Instant currentTime = Instant.now();
            List<ProductSpecs> specs = objectMapper.readValue(
                    form.getSpecs(),
                    new TypeReference<List<ProductSpecs>>() {
                    }
            );
            for (ProductSpecs spec : specs) {
                if (spec.getDataType() == AttributeDataType.SELECT) {
                    String option = objectMapper.convertValue(
                            spec.getValue(),
                            String.class
                    );
                    spec.setValueId(option);
                }
                if (spec.getDataType() == AttributeDataType.MULTI_SELECT) {
                    List<String> options = objectMapper.convertValue(
                            spec.getValue(),
                            new TypeReference<List<String>>() {
                            }
                    );
                    spec.setValueIds(options);
                }
            }
            ProductEntity product = ProductEntity.builder()
                    .name(form.getName())
                    .slug(form.getSlug())
                    .thumbnail(thumbnail)
                    .gallery(galleryImages)
                    .brandId(brandEntity.getId())
                    .hasVariant(form.getHasVariants())
                    .numberOfReviews(0)
                    .averageRating(0.0)
                    .specs(specs)
                    .variantGroups(form.getAttributes())
                    .warrantyMonth(form.getWarrantyMonth())
                    .status(ProductStatus.draft)
                    .categoryId(categoryEntity.getId())
                    .description(form.getDescription())
                    .shortDescription(form.getShortDescription())
                    .createdAt(currentTime)
                    .updatedAt(currentTime)
                    .build();
            ProductEntity updatedProduct = productRepository.save(product);

            List<SkuEntity> skuEntities = new ArrayList<>();
            for (SkuItemForm sku : form.getSkus()) {
                ImageEntity skuImageItem = null;
                if (form.getHasVariants()) {
                    CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(sku.getImage(), "skus");
                    skuIds.add(skuResult.getPublicId());
                    skuImageItem = new ImageEntity(skuResult.getUrl(), skuResult.getPublicId());
                } else {
                    skuImageItem = new ImageEntity(product.getThumbnail().getImageUrl(), product.getThumbnail().getImagePublicId());
                }
                SkuEntity skuItem = SkuEntity.builder()
                        .spuId(updatedProduct.getId())
                        .skuCode(sku.getCode())
                        .name(sku.getName())
                        .thumbnail(skuImageItem)
                        .price(sku.getPrice())
                        .originalPrice(sku.getOriginalPrice())
                        .costPrice(sku.getCostPrice())
                        .stock(sku.getStock())
                        .active(sku.getActive() ? SkuStatus.ACTIVE : SkuStatus.INACTIVE)
                        .discontinued(null)
                        .createdAt(updatedProduct.getCreatedAt())
                        .selections(sku.getSpecs()
                                .stream()
                                .map(sl -> SkuSelect.builder()
                                        .groupId(sl.getGroupId())
                                        .valueId(sl.getId())
                                        .build())
                                .toList())
                        .updatedAt(updatedProduct.getUpdatedAt())
                        .build();
                skuEntities.add(skuItem);
            }
            double min = skuEntities.stream().mapToDouble(SkuEntity::getPrice).min().orElse(0);
            double max = skuEntities.stream().mapToDouble(SkuEntity::getPrice).max().orElse(0);
            updatedProduct.setMinPrice(min);
            updatedProduct.setMaxPrice(max);
            ProductEntity savedProduct = productRepository.save(updatedProduct);
            List<SkuEntity> skuEntityList = skuRepository.saveAll(skuEntities);
            List<ProductCreatedEvent.SkuInitData> skuDataList = skuEntityList.stream()
                    .map(sku -> new ProductCreatedEvent.SkuInitData(
                            sku.getSkuCode(),
                            sku.getStock() != null ? sku.getStock() : 0 // Null check
                    )).toList();
            Instant now = Instant.now();
            /* =========================
             * 1. Payload cho Elasticsearch
             * Event: PRODUCT_UPSERT
             * ========================= */
            Map<String, Object> esPayload = buildProductEsPayload(savedProduct, skuEntityList, brandEntity, categoryEntity);


            /* =========================
             * 2. Payload cho Inventory
             * Event: STOCK_CHANGED (init stock)
             * ========================= */
            Map<String, Object> invPayload = new HashMap<>();
            invPayload.put("productId", savedProduct.getId());
            invPayload.put("reason", "PRODUCT_CREATED");
            invPayload.put("changes", skuEntityList.stream()
                    .map(sku -> Map.of(
                            "skuCode", sku.getSkuCode(),
                            "newStock", sku.getStock() != null ? sku.getStock() : 0,
                            "delta", sku.getStock() != null ? sku.getStock() : 0
                    ))
                    .toList());

            /* =========================
             * 3. Insert Outbox (append-only)
             * ========================= */
            outboxRepository.save(
                    OutboxEventEntity.builder()
                            .aggregateType("Product")
                            .aggregateId(savedProduct.getId())
                            .eventType(OutboxEventType.PRODUCT_CREATED)
                            .idempotencyKey(idemKey + "es")
                            .payloadJson(objectMapper.writeValueAsString(esPayload))
                            .occurredAt(now)
                            .createdAt(now)
                            .build()
            );

            outboxRepository.save(
                    OutboxEventEntity.builder()
                            .aggregateType("Product")
                            .aggregateId(savedProduct.getId())
                            .eventType(OutboxEventType.STOCK_CHANGED)
                            .idempotencyKey(idemKey + "iv")
                            .payloadJson(objectMapper.writeValueAsString(invPayload))
                            .occurredAt(now)
                            .createdAt(now)
                            .build()
            );
        } catch (Exception e) {
            if (StringUtils.hasText(thumbnailId)) {
                cloudinaryService.deleteImage(thumbnailId, "products");
            }
            if (galleryIds != null) {
                cloudinaryService.deleteManyImage(galleryIds);
            }
            if (!skuIds.isEmpty()) {
                cloudinaryService.deleteManyImage(skuIds);
            }
            if (e instanceof JsonProcessingException) {
                throw new BusinessException(VALIDATE_FAIL);
            }
            throw e;
        }
    }

    @Transactional
    @Override
    public void updateProduct(ProductUpdateForm form, String id, String idemKey) throws JsonProcessingException {
        if (!StringUtils.hasText(form.getName())
                || !StringUtils.hasText(form.getDescription())
                || !StringUtils.hasText(form.getSlug())
                || !StringUtils.hasText(form.getShortDescription())
                || !StringUtils.hasText(form.getBrandId())
                || !StringUtils.hasText(form.getCategoryId())
                || form.getSpecs() == null || form.getSpecs().isEmpty()
                || !id.equals(form.getId())
        ) throw new BusinessException(VALIDATE_FAIL);
        String thumbnailId = null;
        List<String> galleryIds = null;
        List<String> skuImageIds = new ArrayList<>();
        ProductEntity product = productRepository.findById(form.getId()).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        try {
            if (form.getThumbnail() != null) {
                CloudinaryUploadResult thumbnailResult = cloudinaryService.uploadImage(form.getThumbnail(), "products");
                thumbnailId = thumbnailResult.getPublicId();
                cloudinaryService.deleteImage(product.getThumbnail().getImagePublicId(), "products");
                product.setThumbnail(new ImageEntity(thumbnailResult.getUrl(), thumbnailResult.getPublicId()));
            }
            List<String> galleryImageIds = product.getGallery().stream()
                    .map(ImageEntity::getImagePublicId)
                    .toList();
            List<String> galleryIdsDelete = galleryImageIds.stream()
                    .filter(url -> !form.getKeptGalleryImageIds().contains(url))
                    .toList();
            if (!galleryIdsDelete.isEmpty()) {
                cloudinaryService.deleteManyImage(galleryIdsDelete);
                List<ImageEntity> newGallery = product.getGallery()
                        .stream()
                        .filter(gl -> !galleryIdsDelete.contains(gl.getImagePublicId()))
                        .toList();
                product.setGallery(newGallery);
            }
            List<ImageEntity> galleryImages = new ArrayList<>();
            if (form.getNewGalleryImages() != null && !form.getNewGalleryImages().isEmpty()) {
                for (MultipartFile imageFile : form.getNewGalleryImages()) {
                    CloudinaryUploadResult galleryResult = cloudinaryService.uploadImage(imageFile, "products");
                    ImageEntity galleryItem = new ImageEntity(galleryResult.getUrl(), galleryResult.getPublicId());
                    galleryImages.add(galleryItem);
                }
                product.setGallery(Stream.concat(product.getGallery().stream(), galleryImages.stream()).toList());
                galleryIds = galleryImages.stream().map(ImageEntity::getImagePublicId).toList();
            }
            if (!form.getCategoryId().equals(product.getCategoryId())) {
                CategoryEntity categoryEntity = categoryRepository.findById(form.getCategoryId()).orElseThrow(
                        () -> new BusinessException(VALIDATE_FAIL)
                );
                product.setCategoryId(categoryEntity.getId());
            }
            if (!form.getBrandId().equals(product.getBrandId())) {
                BrandEntity brandEntity = brandRepository.findById(form.getBrandId()).orElseThrow(
                        () -> new BusinessException(VALIDATE_FAIL)
                );
                product.setBrandId(brandEntity.getId());
            }
            Instant currentTime = Instant.now();
            List<ProductSpecs> specs = objectMapper.readValue(
                    form.getSpecs(),
                    new TypeReference<List<ProductSpecs>>() {
                    }
            );
            for (ProductSpecs spec : specs) {
                if (spec.getDataType() == AttributeDataType.SELECT) {
                    String option = objectMapper.convertValue(
                            spec.getValue(),
                            String.class
                    );
                    spec.setValueId(option);
                }
                if (spec.getDataType() == AttributeDataType.MULTI_SELECT) {
                    List<String> options = objectMapper.convertValue(
                            spec.getValue(),
                            new TypeReference<List<String>>() {
                            }
                    );
                    spec.setValueIds(options);
                }
            }
            product.setName(form.getName());
            product.setSlug(form.getSlug());
            product.setHasVariant(form.getHasVariants());
            product.setWarrantyMonth(form.getWarrantyMonth());
            product.setDescription(form.getDescription());
            product.setShortDescription(form.getShortDescription());
            product.setUpdatedAt(currentTime);
            product.setUpdatedBy(AuthenticationUtils.getUserId());
            product.setVariantGroups(form.getAttributes());
            List<SkuEntity> skus = skuRepository.findAllBySpuId(product.getId());
            List<String> skuUpdateIds = form.getSkus().stream().map(SkuItemForm::getId)
                    .filter(StringUtils::hasText)
                    .toList();
            boolean validateIds = skus.stream().allMatch(sku -> skuUpdateIds.contains(sku.getId()));
            if (!validateIds)
                throw new BusinessException(VALIDATE_FAIL);
            if (form.getSkus().size() < skus.size())
                throw new BusinessException(VALIDATE_FAIL);
            List<SkuEntity> skuEntityList = new ArrayList<>();
            for (SkuItemForm skuItemForm : form.getSkus()) {
                if (!StringUtils.hasText(skuItemForm.getId())) {
                    if (skuItemForm.getImage() == null)
                        throw new BusinessException(VALIDATE_FAIL);
                    CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(skuItemForm.getImage(), "skus");
                    ImageEntity skuImage = new ImageEntity(skuResult.getUrl(), skuResult.getPublicId());
                    skuImageIds.add(skuImage.getImagePublicId());
                    SkuEntity skuEntity = SkuEntity.builder()
                            .name(skuItemForm.getName())
                            .spuId(product.getId())
                            .stock(0)
                            .active(skuItemForm.getActive() ? SkuStatus.ACTIVE : SkuStatus.INACTIVE)
                            .selections(skuItemForm.getSpecs().stream()
                                    .map(sko -> SkuSelect
                                            .builder()
                                            .groupId(sko.getGroupId())
                                            .valueId(sko.getId())
                                            .build())
                                    .toList())
                            .thumbnail(skuImage)
                            .createdAt(Instant.now())
                            .originalPrice(skuItemForm.getOriginalPrice())
                            .price(skuItemForm.getPrice())
                            .skuCode(skuItemForm.getCode())
                            .costPrice(skuItemForm.getCostPrice())
                            .build();
                    skuEntityList.add(skuEntity);
                } else {
                    SkuEntity skuEntity = skus.stream().filter(sk -> sk.getId()
                                    .equals(skuItemForm.getId()))
                            .findFirst()
                            .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
                    if (skuItemForm.getImage() != null) {
                        CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(skuItemForm.getImage(), "skus");
                        skuImageIds.add(skuResult.getPublicId());
                        cloudinaryService.deleteImage(skuEntity.getThumbnail().getImagePublicId(), "skus");
                        skuEntity.setThumbnail(new ImageEntity(skuResult.getUrl(), skuResult.getPublicId()));
                    }
                    skuEntity.setName(skuItemForm.getName());
                    skuEntity.setUpdatedAt(Instant.now());
                    skuEntity.setActive(skuItemForm.getActive() ? SkuStatus.ACTIVE : SkuStatus.INACTIVE);

                    skuEntity.setUpdatedBy(AuthenticationUtils.getUserId());
                    skuEntity.setOriginalPrice(skuItemForm.getOriginalPrice());
                    skuEntity.setPrice(skuItemForm.getPrice());
                    skuEntity.setCostPrice(skuItemForm.getCostPrice());
                    skuEntity.setPrice(skuItemForm.getPrice());
                    skuEntityList.add(skuEntity);
                }
            }
            double min = skuEntityList.stream().mapToDouble(SkuEntity::getPrice).min().orElse(0);
            double max = skuEntityList.stream().mapToDouble(SkuEntity::getPrice).max().orElse(0);
            product.setMinPrice(min);
            product.setMaxPrice(max);
            ProductEntity saved = productRepository.save(product);
            List<SkuEntity> finalSkus = skuRepository.saveAll(skuEntityList);
            CategoryEntity categoryEntity = categoryRepository.findById(saved.getCategoryId())
                    .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
            BrandEntity brandEntity = brandRepository.findById(saved.getBrandId())
                    .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
            Map<String, Object> esPayload = buildProductEsPayload(saved, finalSkus, brandEntity, categoryEntity);
            Instant now = Instant.now();


            outboxRepository.save(
                    OutboxEventEntity.builder()
                            .aggregateType("Product")
                            .aggregateId(saved.getId())
                            .eventType(OutboxEventType.PRODUCT_UPSERT) // hoặc PRODUCT_UPDATED
                            .idempotencyKey(idemKey + "es")           // ensure unique
                            .payloadJson(objectMapper.writeValueAsString(esPayload))
                            .occurredAt(now)
                            .createdAt(now)
                            .build()
            );
        } catch (Exception e) {
            if (StringUtils.hasText(thumbnailId)) {
                cloudinaryService.deleteImage(thumbnailId, "products");
            }
            if (galleryIds != null) {
                cloudinaryService.deleteManyImage(galleryIds);
            }
            if (!skuImageIds.isEmpty()) {
                cloudinaryService.deleteManyImage(skuImageIds);
            }
            if (e instanceof JsonProcessingException) {
                throw new BusinessException(VALIDATE_FAIL);
            }
            throw e;
        }
    }

    @Override
    public void deleteProduct(String id) {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        ProductEntity product = productRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        String url = UriComponentsBuilder
                .fromUriString("http://localhost:8081/products/")
                .path("{id}/order-usage")
                .build(id)
                .toString();
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set(HttpHeaders.AUTHORIZATION, AuthenticationUtils.currentBearerToken());
            HttpEntity<Void> entity = new HttpEntity<>(headers);
            ResponseEntity<OrderExistenceResponse> resp =
                    restTemplate.exchange(url, HttpMethod.GET, entity, OrderExistenceResponse.class);
            List<SkuEntity> skus = skuRepository.findAllBySpuId(id);
            OrderExistenceResponse body = resp.getBody();
            if (body != null && Boolean.TRUE.equals(body.getExists())) {
                product.setStatus(ProductStatus.archived);
                skus.forEach(sku -> {
                    sku.setActive(SkuStatus.ARCHIVED);
                });
                skuRepository.saveAll(skus);
            } else {
                List<String> publicGalleryIds = product.getGallery().stream()
                        .map(ImageEntity::getImagePublicId).toList();
                cloudinaryService.deleteManyImage(publicGalleryIds);
                cloudinaryService.deleteImage(product.getThumbnail().getImagePublicId(), "products");
                productRepository.delete(product);
                List<String> skuImageIds = skus.stream().map(s -> s.getThumbnail().getImagePublicId()).toList();
                cloudinaryService.deleteManyImage(skuImageIds);
                skuRepository.deleteAll(skus);
            }
        } catch (HttpClientErrorException | ResourceAccessException ex) {
            // tuỳ business: fail thì cho xoá hay không?
            log.error("Error during delete product", ex);
            throw new BusinessException(VALIDATE_FAIL);
        }
    }

    @Transactional
    @Override
    public String discontinuedSku(String skuId, String idemKey, DiscontinuedForm form) throws JsonProcessingException {
        if (!StringUtils.hasText(form.getReason()))
            throw new BusinessException(VALIDATE_FAIL);
        SkuEntity sku = skuRepository.findById(skuId).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        sku.setDiscontinued(DiscontinuedType.TEMPORARY);
        sku.setActive(SkuStatus.DISCONTINUED);
        sku.setUpdatedAt(Instant.now());
        sku.setUpdatedBy(AuthenticationUtils.getUserId());
        SkuEntity savedSku = skuRepository.save(sku);
        ProductEntity product = productRepository.findById(savedSku.getSpuId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        List<SkuEntity> activeSkus = skuRepository
                .findAllBySpuIdAndActive(product.getId(), SkuStatus.ACTIVE);
        double min = activeSkus.stream().mapToDouble(SkuEntity::getPrice).min().orElse(0);
        double max = activeSkus.stream().mapToDouble(SkuEntity::getPrice).max().orElse(0);
        product.setMinPrice(min);
        product.setMaxPrice(max);
        productRepository.save(product);
        CategoryEntity category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        BrandEntity brand = brandRepository.findById(product.getBrandId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        Map<String, Object> esPayload = buildProductEsPayload(product, activeSkus, brand, category);
        Instant now = Instant.now();

        outboxRepository.save(
                OutboxEventEntity.builder()
                        .aggregateType("Product")
                        .aggregateId(product.getId())
                        .eventType(OutboxEventType.PRODUCT_UPSERT)
                        .idempotencyKey(idemKey + ":sku-discontinued")
                        .payloadJson(objectMapper.writeValueAsString(esPayload))
                        .occurredAt(now)
                        .createdAt(now)
                        .build()
        );
        return product.getId();
    }

    @Override
    public ProductPdpDTO productDetail(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        ProductEntity product = productRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        List<SkuEntity> skus = skuRepository.findAllBySpuIdAndActive(product.getId(), SkuStatus.ACTIVE);
        CategoryEntity category = categoryRepository.findById(product.getCategoryId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        List<String> ancestorIds = category.getAncestor().stream()
                .map(Ancestors::getId).toList();

        Map<String,CategoryEntity> ancestorMap  = categoryRepository.findByIdIn(ancestorIds)
                .stream()
                .collect(Collectors.toMap(CategoryEntity::getId, ca -> ca));
        BrandEntity brand = brandRepository.findById(product.getBrandId())
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        List<String> attributeIds = product.getSpecs().stream().map(ProductSpecs::getId).toList();
        Map<String, AttributeEntity> attributesMap = attributeRepository.findAllByIdIn(attributeIds)
                .stream().collect(Collectors.toMap(AttributeEntity::getId, attribute -> attribute));

        List<AncestorDTO> ancestorDTOS = new ArrayList<>(category.getAncestor().stream().map(
                ca -> AncestorDTO.builder()
                        .id(ancestorMap.get(ca.getId()).getId())
                        .name(ancestorMap.get(ca.getId()).getName())
                        .slug(ancestorMap.get(ca.getId()).getSlug())
                        .build()
        ).toList());

        ancestorDTOS.add(AncestorDTO.builder()
                        .id(category.getId())
                        .name(category.getName())
                        .slug(category.getSlug())
                .build());
        List<ProductPdpSpecDTO> specs = product.getSpecs().stream()
                .map(sp -> {
                    AttributeDataType dataType = sp.getDataType();
                    String value = null;
                    if (dataType == AttributeDataType.SELECT || dataType == AttributeDataType.MULTI_SELECT)
                        value = attributesMap.get(sp.getId()).getOptions()
                                .stream().filter(o -> o.getId().equals(sp.getValue().toString()))
                                .findFirst().orElse(null).getLabel();
                    else {
                        value = sp.getValue().toString();
                    }
                    return ProductPdpSpecDTO.builder()
                            .id(sp.getId())
                            .label(sp.getLabel())
                            .unit(sp.getUnit())
                            .displayOrder(sp.getDisplayOrder())
                            .value(value)
                            .build();
                })
                .toList();
        return ProductPdpDTO.builder()
                .id(product.getId())
                .slug(product.getSlug())
                .name(product.getName())
                .description(product.getDescription())
                .shortDescription(product.getShortDescription())
                .thumbnail(product.getThumbnail())
                .gallery(product.getGallery())
                .category(ancestorDTOS)
                .brand(BrandPdpDTO.builder()
                        .id(brand.getId())
                        .name(brand.getName())
                        .slug(brand.getSlug())
                        .build())
                .specs(specs)
                .minPrice(product.getMinPrice())
                .maxPrice(product.getMaxPrice())
                .numberOfReviews(product.getNumberOfReviews())
                .averageRating(product.getAverageRating())
                .variantGroups(product.getVariantGroups())
                .warrantyMonth(product.getWarrantyMonth())
                .defaultSkuId(skus.get(0).getId())
                .skus(skus.stream().map(
                        sk -> SkuDTO.builder()
                                .id(sk.getId())
                                .name(sk.getName())
                                .skuCode(sk.getSkuCode())
                                .price(sk.getPrice())
                                .originalPrice(sk.getOriginalPrice())
                                .selections(sk.getSelections())
                                .thumbnail(sk.getThumbnail())
                                .build()
                ).toList())
                .build();
    }

    private Map<String, Object> buildProductEsPayload(ProductEntity product, List<SkuEntity> skuEntities, BrandEntity brandEntity, CategoryEntity categoryEntity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", product.getId());
        payload.put("name", product.getName());
        payload.put("slug", product.getSlug());
        payload.put("brand", Map.of(
                "id", brandEntity.getId(),
                "name", brandEntity.getName(),
                "slug", brandEntity.getSlug()
        ));
        List<String> ancestorIds = new ArrayList<>();
        if (categoryEntity.getAncestor() != null) {
            // TODO: sửa getter theo field thực tế của Ancestors (id/categoryId)
            for (Ancestors a : categoryEntity.getAncestor()) {
                if (a != null && StringUtils.hasText(a.getId())) {
                    ancestorIds.add(a.getId());
                }
            }
        }
        if (!ancestorIds.contains(categoryEntity.getId())) ancestorIds.add(categoryEntity.getId());

        payload.put("category", Map.of(
                "id", categoryEntity.getId(),
                "name", categoryEntity.getName(),
                "slug", categoryEntity.getSlug(),
                "ancestorIds", ancestorIds
        ));
        payload.put("status", product.getStatus().name());
        payload.put("minPrice", product.getMinPrice());
        payload.put("maxPrice", product.getMaxPrice());
        payload.put("shortDescription", product.getShortDescription());
        payload.put("description", product.getDescription()); // nếu ES cần

        if (product.getThumbnail() != null) {
            payload.put("thumbnail", Map.of(
                    "url", product.getThumbnail().getImageUrl(),
                    "publicId", product.getThumbnail().getImagePublicId()
            ));
        } else {
            payload.put("thumbnail", null);
        }

        if (product.getGallery() != null) {
            payload.put("gallery", product.getGallery().stream()
                    .map(img -> Map.of(
                            "url", img.getImageUrl(),
                            "publicId", img.getImagePublicId()
                    ))
                    .toList());
        } else {
            payload.put("gallery", List.of());
        }

        payload.put("specs", product.getSpecs());           // bạn đang lưu specs dạng List<ProductSpecs>
        payload.put("variantGroups", product.getVariantGroups()); // nếu ES đang dùng
        payload.put("hasVariant", product.getHasVariant());
        payload.put("warrantyMonth", product.getWarrantyMonth());
        payload.put("averageRating", product.getAverageRating());
        payload.put("numberOfReviews", product.getNumberOfReviews());

        // SKU list
        payload.put("skus", skuEntities.stream()
                .map(sku -> {
                    Map<String, Object> m = new HashMap<>();
                    m.put("skuId", sku.getId());
                    m.put("skuCode", sku.getSkuCode());
                    m.put("name", sku.getName());
                    m.put("price", sku.getPrice());
                    m.put("originalPrice", sku.getOriginalPrice());
                    m.put("costPrice", sku.getCostPrice());
                    // ✅ boolean để khớp mapping
                    m.put("active", sku.getActive() == SkuStatus.ACTIVE);
                    // ✅ lưu thêm status enum nếu muốn filter/debug
                    m.put("status", sku.getActive().name());

                    if (sku.getThumbnail() != null) {
                        m.put("thumbnail", Map.of(
                                "url", sku.getThumbnail().getImageUrl(),
                                "publicId", sku.getThumbnail().getImagePublicId()
                        ));
                    } else {
                        m.put("thumbnail", null);
                    }

                    // nếu ES cần selections
                    m.put("selections", sku.getSelections());

                    // nếu bạn có discontinuedAt
                    return m;
                })
                .toList());

        return payload;
    }
}
