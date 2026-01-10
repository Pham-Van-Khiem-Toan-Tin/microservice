package com.ecommerce.catalogservice.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductSpecs {
    private String id;
    private String code;
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private Integer displayOrder;
    private Object value;
    private String valueId;
    private List<String> valueIds;
}
