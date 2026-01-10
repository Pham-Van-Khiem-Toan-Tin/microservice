package com.ecommerce.catalogservice.entity;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductVariantGroup {
    private String id;
    private String label;
    private List<ProductOptionItem> values;
}
