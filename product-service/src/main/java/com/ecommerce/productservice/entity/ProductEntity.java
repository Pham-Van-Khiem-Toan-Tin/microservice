package com.ecommerce.productservice.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document("products")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@Data
public class ProductEntity {
    @Id
    private String id;
    private String title;
    private String slug;
    private String categoryId;
    private String primaryImage;
    private String description;
    private int numOfReviews;
    private double avgStar;
    private List<ProductItemEntity> SKUs;
}
