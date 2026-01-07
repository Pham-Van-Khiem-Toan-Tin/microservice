package com.ecommerce.catalogservice.dto.request.product;

import com.ecommerce.catalogservice.entity.ProductSpecs;
import com.ecommerce.catalogservice.entity.ProductVariantGroup;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
public class ProductCreateForm {
    private String name;
    private String slug;
    private String brandId;
    private String categoryId;
    private List<ProductSpecs> specs;
    private List<ProductVariantGroup> attributes;
    private String description;
    private String shortDescription;
    private Boolean hasVariants;
    private MultipartFile thumbnail;
    private List<MultipartFile> gallery;

    private List<SkuItemForm> skus;
}
