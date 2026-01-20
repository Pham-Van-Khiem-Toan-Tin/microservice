package com.ecommerce.searchservice.dto.product;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class ProductListItem {
    private String productId;
    private String name;
    private String slug;
    private Integer numberOfReviews;
    private Double averageRating;
    private ImageRef thumbnail;

    // BigDecimal xử lý cực tốt số dạng 2.489E7
    private BigDecimal minPrice;
    private BigDecimal maxPrice;

    private BrandRef brand;
    private CategoryRef category;

    private List<VariantGroup> variantGroups;
    private List<ProductSku> skus;

    // Có thể lấy thêm các trường khác nếu cần hiển thị
    private String shortDescription;
    private List<SpecItem> specs;
}
