package com.ecommerce.catalogservice.dto.response.product;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ProductReviewResponse {
    private String id;
    private String userName;
    private String userAvatar;
    private int rating;
    private String comment;
    private String skuAttributes;
    private LocalDateTime createdAt;
}
