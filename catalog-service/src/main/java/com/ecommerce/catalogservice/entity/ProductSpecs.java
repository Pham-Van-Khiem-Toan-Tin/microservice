package com.ecommerce.catalogservice.entity;

import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecs {
    private String code;
    private String label;
    private Object value;
    private String unit;
}
