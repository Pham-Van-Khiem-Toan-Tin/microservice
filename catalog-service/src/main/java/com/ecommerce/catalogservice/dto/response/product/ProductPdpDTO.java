package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.entity.ImageEntity;
import com.ecommerce.catalogservice.entity.ProductVariantGroup;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductPdpDTO {
    private String id;
    private String slug;
    private String name;
    private String shortDescription;
    private String description;
    private ImageEntity thumbnail;
    private Integer warrantyMonth;
    private BrandPdpDTO brand;
    private List<AncestorDTO> category;
    private Double minPrice;
    private Double maxPrice;
    private String defaultSkuId;
    private Integer numberOfReviews;
    private Double averageRating;
    private List<ImageEntity> gallery;
    private List<ProductPdpSpecDTO> specs;
    private List<ProductVariantGroup> variantGroups;
    private List<SkuDTO> skus;
}
