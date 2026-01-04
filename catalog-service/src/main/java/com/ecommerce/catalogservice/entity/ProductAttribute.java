package com.ecommerce.catalogservice.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductAttribute {
    private String id;
    private String code;
    private String label;
    private String value;
    private String labelOption;
    private String unit;
}
