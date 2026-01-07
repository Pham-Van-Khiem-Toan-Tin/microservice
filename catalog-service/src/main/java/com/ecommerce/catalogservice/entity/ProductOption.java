package com.ecommerce.catalogservice.entity;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductOption {
    private String id;
    private String label;
    private Boolean active;
    private List<ProductOptionItem> values;
}
