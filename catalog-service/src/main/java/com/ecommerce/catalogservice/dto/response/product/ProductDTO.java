package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.entity.ProductStatus;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductDTO {
    private String id;
    private String name;
    private String slug;
    private Category category;
    private Brand brand;
    private ProductStatus status;
}
