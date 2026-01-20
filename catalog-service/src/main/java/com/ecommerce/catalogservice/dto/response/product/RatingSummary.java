package com.ecommerce.catalogservice.dto.response.product;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class RatingSummary {
    private double averageRating;
    private long totalReviews;
    private Map<Integer, Long> ratingBreakdown;
}
