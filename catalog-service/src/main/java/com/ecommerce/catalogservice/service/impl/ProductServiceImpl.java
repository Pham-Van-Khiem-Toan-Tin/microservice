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
                    new TypeReference<List<ProductSpecs>>() {}
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
                            new TypeReference<List<String>>() {}
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
                        .originalPrice(sku.getPrice())
                        .costPrice(sku.getCostPrice())
                        .stock(sku.getStock())
                        .active(sku.getActive())
                        .discontinued(false)
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
            Map<String, Object> esPayload = new HashMap<>();
            esPayload.put("productId", savedProduct.getId());
            esPayload.put("name", savedProduct.getName());
            esPayload.put("slug", savedProduct.getSlug());
            esPayload.put("brandId", savedProduct.getBrandId());
            esPayload.put("categoryId", savedProduct.getCategoryId());
            esPayload.put("status", savedProduct.getStatus().name());
            esPayload.put("minPrice", savedProduct.getMinPrice());
            esPayload.put("maxPrice", savedProduct.getMaxPrice());
            esPayload.put("shortDescription", savedProduct.getShortDescription());

            esPayload.put("thumbnail", Map.of(
                    "url", savedProduct.getThumbnail().getImageUrl(),
                    "publicId", savedProduct.getThumbnail().getImagePublicId()
            ));

            esPayload.put("gallery", savedProduct.getGallery().stream()
                    .map(img -> Map.of(
                            "url", img.getImageUrl(),
                            "publicId", img.getImagePublicId()
                    ))
                    .toList());

            esPayload.put("specs", savedProduct.getSpecs());

            esPayload.put("skus", skuEntityList.stream()
                    .map(sku -> Map.of(
                            "skuId", sku.getId(),
                            "skuCode", sku.getSkuCode(),
                            "name", sku.getName(),
                            "price", sku.getPrice(),
                            "originalPrice", sku.getOriginalPrice(),
                            "active", sku.getActive()
                    ))
                    .toList());

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
    public void updateProduct(ProductUpdateForm form, String id) throws JsonProcessingException {
        if (!StringUtils.hasText(id)) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        ProductEntity product = productRepository.findById(id).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        if (StringUtils.hasText(form.getCategoryId())) {
            categoryRepository.findById(form.getCategoryId()).orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
            product.setCategoryId(form.getCategoryId());
        }
        if (StringUtils.hasText(form.getBrandId())) {
            brandRepository.findById(form.getBrandId()).orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
            product.setBrandId(form.getBrandId());
        }
        if (StringUtils.hasText(form.getName())) product.setName(form.getName());
        if (StringUtils.hasText(form.getSlug())) product.setSlug(form.getSlug());
        if (StringUtils.hasText(form.getDescription())) product.setDescription(form.getDescription());
        if (StringUtils.hasText(form.getShortDescription())) product.setShortDescription(form.getShortDescription());
        if (form.getWarrantyMonth() != null) product.setWarrantyMonth(form.getWarrantyMonth());
        if (form.getStatus() != null) product.setStatus(form.getStatus());
        if (form.getHasVariants() != null) product.setHasVariant(form.getHasVariants());
        if (form.getAttributes() != null) product.setVariantGroups(form.getAttributes());
        if (StringUtils.hasText(form.getSpecs())) {
            List<ProductSpecs> specs = objectMapper.readValue(
                    form.getSpecs(),
                    new TypeReference<List<ProductSpecs>>() {
                    }
            );
            product.setSpecs(specs);
        }
        product.setUpdatedAt(Instant.now());
        if (form.getThumbnail() != null && !form.getThumbnail().isEmpty()) {
            // Upload ảnh mới
            CloudinaryUploadResult newThumbResult = cloudinaryService.uploadImage(form.getThumbnail(), "products");

            // Xóa ảnh cũ trên Cloudinary (nếu tồn tại)
            if (product.getThumbnail() != null && StringUtils.hasText(product.getThumbnail().getImagePublicId())) {
                cloudinaryService.deleteImage(product.getThumbnail().getImagePublicId(), "products");
            }

            // Set ảnh mới
            product.setThumbnail(new ImageEntity(newThumbResult.getUrl(), newThumbResult.getPublicId()));
        }

        // 5. Xử lý Gallery
        List<ImageEntity> currentGallery = product.getGallery() != null ? product.getGallery() : new ArrayList<>();

        // 5a. Xóa ảnh cũ theo request
//        if (form.getGallery() != null && !form.getGallery().isEmpty()) {
//            // Lọc ra các ảnh cần xóa để gọi Cloudinary delete
//            List<String> idsToDelete = form.getDeletedGalleryIds();
//            cloudinaryService.deleteManyImage(idsToDelete); // Xóa trên Cloud
//
//            // Xóa trong List object
//            currentGallery.removeIf(img -> idsToDelete.contains(img.getImagePublicId()));
//        }
//
//        // 5b. Thêm ảnh mới vào Gallery
//        if (form.getNewGalleryImages() != null && !form.getNewGalleryImages().isEmpty()) {
//            for (MultipartFile file : form.getNewGalleryImages()) {
//                CloudinaryUploadResult galleryResult = cloudinaryService.uploadImage(file, "products");
//                currentGallery.add(new ImageEntity(galleryResult.getUrl(), galleryResult.getPublicId()));
//            }
//        }
//        product.setGallery(currentGallery);
//
//        // Lưu Product trước để lấy ID (dù ID đã có, nhưng để chắc chắn transaction flow)
//        ProductEntity savedProduct = productRepository.save(product);
//
//        // 6. Xử lý SKU
//        if (form.getSkus() != null && !form.getSkus().isEmpty()) {
//            List<SkuEntity> skuEntitiesToSave = new ArrayList<>();
//
//            for (ProductUpdateForm.SkuUpdateItem skuItemForm : form.getSkus()) {
//                SkuEntity skuEntity;
//
//                // Case A: Update SKU cũ
//                if (StringUtils.hasText(skuItemForm.getId())) {
//                    skuEntity = skuRepository.findById(skuItemForm.getId()).orElse(null);
//                    if (skuEntity == null) continue; // Skip nếu ID rác
//
//                    if (StringUtils.hasText(skuItemForm.getName())) skuEntity.setName(skuItemForm.getName());
//                    if (StringUtils.hasText(skuItemForm.getCode())) skuEntity.setSkuCode(skuItemForm.getCode());
//                    if (skuItemForm.getPrice() != null) skuEntity.setPrice(skuItemForm.getPrice());
//                    if (skuItemForm.getStock() != null) skuEntity.setStock(skuItemForm.getStock());
//                    if (skuItemForm.getActive() != null) skuEntity.setActive(skuItemForm.getActive());
//                    if (skuItemForm.getSelections() != null) skuEntity.setSelections(skuItemForm.getSelections());
//
//                    // Update ảnh SKU nếu có file mới
//                    if (skuItemForm.getImage() != null && !skuItemForm.getImage().isEmpty()) {
//                        // Xóa ảnh cũ SKU nếu khác ảnh thumbnail gốc
//                        if (skuEntity.getImage() != null
//                                && !skuEntity.getImage().getImagePublicId().equals(savedProduct.getThumbnail().getImagePublicId())) {
//                            cloudinaryService.deleteImage(skuEntity.getImage().getImagePublicId(), "skus");
//                        }
//                        CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(skuItemForm.getImage(), "skus");
//                        skuEntity.setImage(new ImageEntity(skuResult.getUrl(), skuResult.getPublicId()));
//                    }
//
//                } else {
//                    // Case B: Tạo SKU Mới
//                    ImageEntity skuImageItem;
//                    if (skuItemForm.getImage() != null && !skuItemForm.getImage().isEmpty()) {
//                        CloudinaryUploadResult skuResult = cloudinaryService.uploadImage(skuItemForm.getImage(), "skus");
//                        skuImageItem = new ImageEntity(skuResult.getUrl(), skuResult.getPublicId());
//                    } else {
//                        // Nếu không up ảnh riêng, dùng thumbnail của Product
//                        skuImageItem = new ImageEntity(savedProduct.getThumbnail().getImageUrl(), savedProduct.getThumbnail().getImagePublicId());
//                    }
//
//                    skuEntity = SkuEntity.builder()
//                            .spuId(savedProduct.getId())
//                            .skuCode(skuItemForm.getCode())
//                            .name(skuItemForm.getName())
//                            .image(skuImageItem)
//                            .price(skuItemForm.getPrice() != null ? skuItemForm.getPrice() : 0.0)
//                            .originalPrice(skuItemForm.getPrice() != null ? skuItemForm.getPrice() : 0.0)
//                            .stock(skuItemForm.getStock() != null ? skuItemForm.getStock() : 0)
//                            .active(true)
//                            .discontinued(false)
//                            .soldCount(0L)
//                            .createdAt(Instant.now())
//                            .build();
//                }
//                skuEntity.setUpdatedAt(Instant.now());
//                skuEntitiesToSave.add(skuEntity);
//            }

//            List<SkuEntity> savedSkus = skuRepository.saveAll(skuEntitiesToSave);
//
//            // Cập nhật lại Min/Max price cho Product
//            double min = savedSkus.stream().mapToDouble(SkuEntity::getPrice).min().orElse(0);
//            double max = savedSkus.stream().mapToDouble(SkuEntity::getPrice).max().orElse(0);
//            savedProduct.setMinPrice(min);
//            savedProduct.setMaxPrice(max);
//            productRepository.save(savedProduct);
//
//            // Gửi Kafka Event (Product Updated)
//            // Lưu ý: Có thể bạn cần tạo event class riêng hoặc dùng lại cấu trúc map
//            // Ở đây mình tái sử dụng ProductCreatedEvent.SkuInitData cho đơn giản, hoặc bạn gửi full DTO
//            List<ProductCreatedEvent.SkuInitData> skuDataList = savedSkus.stream()
//                    .map(sku -> new ProductCreatedEvent.SkuInitData(
//                            sku.getSkuCode(),
//                            sku.getStock()
//                    )).toList();
//            ProductCreatedEvent event = new ProductCreatedEvent(savedProduct.getId(), skuDataList);
//            // Kafka topic: có thể dùng chung created hoặc topic update riêng
//            kafkaTemplate.send("catalog.product.updated", savedProduct.getId(), event);
//        }
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
                    sku.setActive(false);
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

}
