package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.product.ReviewForm;
import com.ecommerce.catalogservice.dto.response.product.ProductReviewResponse;
import com.ecommerce.catalogservice.dto.response.product.RatingSummary;
import com.ecommerce.catalogservice.entity.ProductReviewEntity;
import com.ecommerce.catalogservice.entity.ReviewStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductReviewService {
    Page<ProductReviewEntity> getReviewsForAdmin(
            ReviewStatus status,
            Integer rating,
            String productId,
            Pageable pageable);
    void updateReviewStatus(String id, ReviewStatus status);
    RatingSummary getRatingSummary(String productId);
    Page<ProductReviewResponse> getProductReviews(String productId, Integer rating, Pageable pageable);
    void createReviews(List<ReviewForm> requests) throws JsonProcessingException;
}
