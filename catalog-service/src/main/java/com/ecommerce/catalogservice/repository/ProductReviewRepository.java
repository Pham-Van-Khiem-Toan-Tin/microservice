package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductReviewEntity;
import com.ecommerce.catalogservice.entity.ReviewStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductReviewRepository extends MongoRepository<ProductReviewEntity, String> {

    // Lấy review của một sản phẩm (chỉ lấy những cái đã được duyệt)
    Page<ProductReviewEntity> findByProductIdAndStatusOrderByCreatedAtDesc(
            String productId, ReviewStatus status, Pageable pageable);

    boolean existsByOrderItemId(String orderItemId);
}
