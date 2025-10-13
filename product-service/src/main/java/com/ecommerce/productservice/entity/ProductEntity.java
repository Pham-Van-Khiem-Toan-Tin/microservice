package com.ecommerce.productservice.entity;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.experimental.PackagePrivate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;

@Document("product")
@NoArgsConstructor
@Builder
@AllArgsConstructor
@PackagePrivate
public class ProductEntity {
    String id;
    String name;
    Double price;
    Set<String> images;
    String brand;
    Integer stock;
    Set<String> categories;
}
