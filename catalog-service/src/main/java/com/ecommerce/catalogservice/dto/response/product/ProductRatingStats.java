package com.ecommerce.catalogservice.dto.response.product;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ProductRatingStats {
    private String id;

    private Double averageRating;
    private Long totalReviews;
}
