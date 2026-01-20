package com.ecommerce.catalogservice.controller;


import com.ecommerce.catalogservice.dto.request.brand.BrandSearchField;
import com.ecommerce.catalogservice.dto.request.review.ReviewSearchField;
import com.ecommerce.catalogservice.dto.response.BrandDTOS;
import com.ecommerce.catalogservice.dto.response.product.ProductReviewResponse;
import com.ecommerce.catalogservice.dto.response.product.RatingSummary;
import com.ecommerce.catalogservice.entity.ProductReviewEntity;
import com.ecommerce.catalogservice.entity.ReviewStatus;
import com.ecommerce.catalogservice.service.ProductReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/reviews")
public class ReviewController {
    @Autowired
    private ProductReviewService reviewService;
    @GetMapping
    public Page<ProductReviewEntity> getReviews(
            @RequestParam(required = false) ReviewStatus status,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String productId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return reviewService.getReviewsForAdmin(status, rating, productId, pageable);
    }
    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> changeStatus(
            @PathVariable String id,
            @RequestParam ReviewStatus status) {
        reviewService.updateReviewStatus(id, status);
        return ResponseEntity.ok().build();
    }
    @GetMapping("/product/{productId}/summary")
    public RatingSummary getRatingSummary(@PathVariable String productId) {
        return reviewService.getRatingSummary(productId);
    }
    @GetMapping("/product/{productId}")
    public Page<ProductReviewResponse> getProductReviews(
            @PathVariable String productId,
            @RequestParam(required = false) Integer rating,
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        return reviewService.getProductReviews(productId, rating, pageable);
    }

}
