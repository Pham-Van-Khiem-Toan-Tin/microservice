package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.dto.response.sku.SkuDetailDTO;
import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.entity.ProductSpecs;
import com.ecommerce.catalogservice.entity.ProductStatus;
import com.ecommerce.catalogservice.entity.ProductVariantGroup;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductDetailDTO {
    private String id;
    private String name;
    private String slug;
    private Category category;
    private Brand brand;
    private Integer warrantyMonth;
    private Boolean hasVariants;
    private List<ProductVariantGroup> variantGroups;
    private List<ProductSpecs> specs;
    private Integer numOfReviews;
    private Double avgRating;
    private ImageEntity thumbnail;
    private List<ImageEntity> gallery;
    private String description;
    private String shortDescription;
    private ProductStatus status;
    private Double minPrice;
    private Double maxPrice;
    private List<SkuDetailDTO> skus;
}
