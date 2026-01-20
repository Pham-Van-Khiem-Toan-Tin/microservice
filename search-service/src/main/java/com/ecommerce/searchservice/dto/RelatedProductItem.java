package com.ecommerce.searchservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.PackagePrivate;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@PackagePrivate
public class RelatedProductItem {
    String id;
    String name;
    String slug;
    String imageUrl;
    Long minPrice;
    Long maxPrice;
    String brandName;
    String brandSlug;
    Double rating;      // <--- Mới
    Integer ratingCount;
    List<Map<String, Object>> variantGroups;
    List<Map<String, Object>> skus;
}
