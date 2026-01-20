package com.ecommerce.searchservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.PackagePrivate;

@Getter
@Setter
@Builder
@PackagePrivate
@AllArgsConstructor
public class WishlistProductDto {
    String id;
    String name;
    String slug;
    String imageUrl;
    Long price;
    Long originalPrice;
    Double rating;
    String brandName;
}
