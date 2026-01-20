package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.request.product.ReviewForm;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.dto.response.product.ProductRatingStats;
import com.ecommerce.catalogservice.dto.response.product.ProductReviewResponse;
import com.ecommerce.catalogservice.dto.response.product.RatingSummary;
import com.ecommerce.catalogservice.dto.response.user.UserResponse;
import com.ecommerce.catalogservice.entity.*;
import com.ecommerce.catalogservice.integration.IdentityFeignClient;
import com.ecommerce.catalogservice.repository.*;
import com.ecommerce.catalogservice.service.ProductReviewService;
import com.ecommerce.catalogservice.utils.AuthenticationUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.GroupOperation;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ecommerce.catalogservice.constants.Constants.*;

@Service
public class ProductReviewServiceImpl implements ProductReviewService {
    @Autowired
    private ProductReviewRepository reviewRepository;
    @Autowired
    private MongoTemplate mongoTemplate;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private SkuRepository skuRepository;
    @Autowired
    private BrandRepository  brandRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private IdentityFeignClient  identityFeignClient;

    @Override
    public Page<ProductReviewEntity> getReviewsForAdmin(ReviewStatus status, Integer rating, String productId, Pageable pageable) {
        Criteria criteria = new Criteria();

        // 1. Phân loại theo Trạng thái (PENDING, APPROVED, REJECTED)
        if (status != null) {
            criteria.and("status").is(status);
        }

        // 2. Lọc theo số sao (1-5)
        if (rating != null && rating > 0) {
            criteria.and("rating").is(rating);
        }

        // 3. Lọc theo mã sản phẩm
        if (productId != null && !productId.isBlank()) {
            criteria.and("productId").is(productId);
        }

        Query query = new Query(criteria).with(pageable);
        List<ProductReviewEntity> list = mongoTemplate.find(query, ProductReviewEntity.class);
        long total = mongoTemplate.count(new Query(criteria), ProductReviewEntity.class);

        return new PageImpl<>(list, pageable, total);
    }

    @Override
    public void updateReviewStatus(String id, ReviewStatus status) {
        ProductReviewEntity review = reviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Review không tồn tại"));
        review.setStatus(status);
        review.setUpdatedAt(LocalDateTime.now());
        reviewRepository.save(review);
    }

    @Override
    public RatingSummary getRatingSummary(String productId) {
        MatchOperation match = Aggregation.match(
                Criteria.where("productId").is(productId).and("status").is(ReviewStatus.APPROVED)
        );

        // 2. Pipeline: Group theo số sao và đếm
        GroupOperation group = Aggregation.group("rating").count().as("count");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<org.bson.Document> results = mongoTemplate.aggregate(
                aggregation, "reviews", org.bson.Document.class
        );

        // 3. Xử lý kết quả
        Map<Integer, Long> breakdown = new HashMap<>();
        for (int i = 1; i <= 5; i++) breakdown.put(i, 0L); // Khởi tạo mặc định 0

        long totalReviews = 0;
        double totalPoints = 0;

        for (org.bson.Document doc : results.getMappedResults()) {
            int rating = doc.getInteger("_id");
            long count = ((Number) doc.get("count")).longValue();
            breakdown.put(rating, count);
            totalReviews += count;
            totalPoints += (rating * count);
        }

        double averageRating = totalReviews > 0 ? totalPoints / totalReviews : 0;

        return RatingSummary.builder()
                .averageRating(Math.round(averageRating * 10.0) / 10.0) // Làm tròn 1 chữ số (vd: 4.5)
                .totalReviews(totalReviews)
                .ratingBreakdown(breakdown)
                .build();
    }

    @Override
    public Page<ProductReviewResponse> getProductReviews(String productId, Integer rating, Pageable pageable) {
        Criteria criteria = Criteria.where("productId").is(productId)
                .and("status").is(ReviewStatus.APPROVED);

        if (rating != null && rating >= 1 && rating <= 5) {
            criteria.and("rating").is(rating);
        }
        Query query = new Query(criteria).with(pageable);
        List<ProductReviewEntity> reviews = mongoTemplate.find(query, ProductReviewEntity.class);

        // 3. Đếm tổng số bản ghi (không dùng phân trang để tính tổng)
        long total = mongoTemplate.count(query.skip(-1).limit(-1), ProductReviewEntity.class);

//         4. Map sang Response và làm mờ tên user
        List<ProductReviewResponse> content = reviews.stream()
                .map(this::convertToResponse)
                .toList();

        return new PageImpl<>(content, pageable, total);
    }
    private ProductReviewResponse convertToResponse(ProductReviewEntity entity) {
        return ProductReviewResponse.builder()
                .id(entity.getId())
                .userName(maskName(entity.getUserName())) // "Nguyễn Văn A" -> "N***A"
                .userAvatar(entity.getUserAvatar())
                .rating(entity.getRating())
                .comment(entity.getComment())
                .skuAttributes(entity.getSkuAttributes())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    private String maskName(String name) {
        if (name == null || name.isBlank()) return "Người dùng ẩn danh";
        if (name.length() < 3) return name.charAt(0) + "***";
        return name.charAt(0) + "***" + name.charAt(name.length() - 1);
    }
    @Transactional
    @Override
    public void createReviews(List<ReviewForm> requests) throws JsonProcessingException {
        if (requests == null || requests.isEmpty()) {
            throw new BusinessException(REVIEW_VALIDATE);
        }
        UserResponse userResponse = identityFeignClient.getUserProfile();
        List<ProductReviewEntity> savedReviews = processAndSaveReviews(requests, userResponse);
        Set<String> productIds = requests.stream()
                .map(ReviewForm::getProductId)
                .collect(Collectors.toSet());
        Map<String, ProductRatingStats> allStats = calculateBatchRating(productIds);
        List<ProductEntity> products = productRepository.findAllById(productIds);
        List<SkuEntity> allSkus = skuRepository.findAllBySpuIdIn(productIds);
        Map<String, List<SkuEntity>> skusByProduct = allSkus.stream()
                .collect(Collectors.groupingBy(SkuEntity::getSpuId));
        Set<String> brandIds = products.stream().map(ProductEntity::getBrandId).collect(Collectors.toSet());
        Set<String> categoryIds = products.stream().map(ProductEntity::getCategoryId).collect(Collectors.toSet());
        Map<String, BrandEntity> brandMap = brandRepository.findAllByIdIn(brandIds)
                .stream().collect(Collectors.toMap(BrandEntity::getId, brand -> brand));
        Map<String, CategoryEntity> categoryMap = categoryRepository.findAllByIdIn(categoryIds)
                .stream().collect(Collectors.toMap(CategoryEntity::getId, ca -> ca));
        List<OutboxEventEntity> outboxEvents = new ArrayList<>();
        Instant now = Instant.now();

        for (ProductReviewEntity review : savedReviews) {
            Map<String, Object> orPayload = buildOrderPayload(review);
            outboxEvents.add(OutboxEventEntity.builder()
                    .aggregateType("review") // Chú ý: Aggregate là Order
                    .aggregateId(review.getOrderId())
                    .eventType(OutboxEventType.REVIEW_CREATED)
                    .idempotencyKey(UUID.randomUUID().toString())
                    .payloadJson(objectMapper.writeValueAsString(orPayload))
                    .occurredAt(Instant.now())
                    .build());
        }
        for (ProductEntity product : products) {
            ProductRatingStats stats = allStats.get(product.getId());
            if (stats != null) {
                product.setNumberOfReviews(stats.getTotalReviews().intValue());
                product.setAverageRating(BigDecimal.valueOf(stats.getAverageRating())
                        .setScale(1, RoundingMode.HALF_UP));
                product.setUpdatedAt(now);

                // 5. BUILD FULL PAYLOAD TẠI ĐÂY
                BrandEntity brand = brandMap.get(product.getBrandId());
                CategoryEntity category = categoryMap.get(product.getCategoryId());
                List<SkuEntity> productSkus = skusByProduct.getOrDefault(product.getId(), Collections.emptyList());
                Map<String, Object> esPayload = buildProductEsPayload(product, productSkus, brand, category);
                outboxEvents.add(OutboxEventEntity.builder()
                        .aggregateType("Product")
                        .aggregateId(product.getId())
                        .eventType(OutboxEventType.PRODUCT_UPSERT) // Dùng chung type để ES dễ xử lý
                        .idempotencyKey("REV-" + product.getId() + "-" + now.toEpochMilli())
                        .payloadJson(objectMapper.writeValueAsString(esPayload))
                        .occurredAt(now)
                        .createdAt(now)
                        .build());
            }
        }

        // 6. Lưu hàng loạt vào SQL (Batch Update)
        productRepository.saveAll(products);
        outboxRepository.saveAll(outboxEvents);
    }
    private Map<String, Object> buildOrderPayload(ProductReviewEntity review) {
        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("orderItemId", review.getOrderItemId());
        orderPayload.put("orderId", review.getOrderId());
        orderPayload.put("isReviewed", true);
        return orderPayload;
    }
    private Map<String, ProductRatingStats> calculateBatchRating(Set<String> productIds) {
        MatchOperation match = Aggregation.match(
                Criteria.where("productId").in(productIds).and("status").is(ReviewStatus.APPROVED)
        );

        GroupOperation group = Aggregation.group("productId")
                .avg("rating").as("averageRating")
                .count().as("totalReviews");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<ProductRatingStats> results = mongoTemplate.aggregate(
                aggregation, "product_reviews", ProductRatingStats.class
        );

        // Chuyển kết quả về Map để dễ truy xuất
        return results.getMappedResults().stream()
                .collect(Collectors.toMap(ProductRatingStats::getId, stats -> stats));
    }
    private List<ProductReviewEntity> processAndSaveReviews(List<ReviewForm> requests, UserResponse userResponse) {

        List<ProductReviewEntity> reviewsToSave = new ArrayList<>();
        String userId = AuthenticationUtils.getUserId();
        for (ReviewForm req : requests) {
            // 1. Kiểm tra thủ công thay cho Annotation
            validateRequest(req);

            // 2. Chặn đánh giá trùng lặp cho từng món hàng trong đơn
            if (reviewRepository.existsByOrderItemId(req.getOrderItemId())) {
                // Có thể bỏ qua hoặc báo lỗi tùy bạn, ở đây mình chọn bỏ qua item đã review
                continue;
            }

            // 3. Map sang Entity
            ProductReviewEntity review = ProductReviewEntity.builder()
                    .productId(req.getProductId())
                    .skuId(req.getSkuId())
                    .userId(userId)
                    .userAvatar(userResponse.getAvatarUrl())
                    .userName(userResponse.getFullName())
                    .orderId(req.getOrderId())
                    .orderItemId(req.getOrderItemId())
                    .rating(req.getRating())
                    .comment(req.getComment())
                    .skuAttributes(req.getSkuAttributes())
                    .status(ReviewStatus.PENDING) // Mặc định chờ duyệt
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();

            reviewsToSave.add(review);
        }
        return reviewRepository.saveAll(reviewsToSave);
    }
    private Map<String, Object> buildProductEsPayload(ProductEntity product, List<SkuEntity> skuEntities, BrandEntity brandEntity, CategoryEntity categoryEntity) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("productId", product.getId());
        payload.put("name", product.getName());
        payload.put("slug", product.getSlug());
        payload.put("brand", Map.of(
                "id", brandEntity.getId(),
                "name", brandEntity.getName(),
                "slug", brandEntity.getSlug(),
                "thumbnail", brandEntity.getLogo().getImageUrl()
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
                "ancestorIds", ancestorIds,
                "thumbnail", categoryEntity.getImage().getImageUrl()
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
    private void validateRequest(ReviewForm req) {
        if (req.getProductId() == null || req.getProductId().isBlank())
            throw new RuntimeException("ProductId không được để trống");
        if (req.getOrderItemId() == null || req.getOrderItemId().isBlank())
            throw new RuntimeException("OrderItemId không được để trống");
        if (req.getRating() == null || req.getRating() < 1 || req.getRating() > 5)
            throw new RuntimeException("Rating phải từ 1 đến 5 sao");
        if (req.getComment() == null || req.getComment().isBlank())
            throw new RuntimeException("Nội dung đánh giá không được để trống");
    }
}
