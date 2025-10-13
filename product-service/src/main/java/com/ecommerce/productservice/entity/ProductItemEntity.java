package com.ecommerce.productservice.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductItemEntity {
    private String SKU;
    private int qt_in_stock;
    private Set<ImageEntity> images;
    private double price;
    private Set<VariationOptionEntity> variations;
}
