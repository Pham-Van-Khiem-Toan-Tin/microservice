package com.ecommerce.searchservice.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.experimental.PackagePrivate;

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
}
