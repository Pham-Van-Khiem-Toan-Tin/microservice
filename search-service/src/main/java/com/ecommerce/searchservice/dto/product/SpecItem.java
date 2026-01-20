package com.ecommerce.searchservice.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class SpecItem {
    private String id;
    private String label;
    private String value;
    private String code;
    private String dataType;
    private String unit;
    private Integer displayOrder;
    private List<String> valueId;
}
