package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.ProductAttribute;
import com.ecommerce.catalogservice.entity.ProductStatus;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

import java.util.List;

@Data
public class ProductCreateForm {
    private String name;
    private String slug;
    private String brandId;
    private String categoryId;
    private List<ProductAttribute> attributes;
    private String description;
    private String shortDescription;

    private ProductStatus status;

}
