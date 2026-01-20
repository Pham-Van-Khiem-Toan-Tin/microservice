package com.ecommerce.searchservice.dto.product;

import lombok.Data;

import java.util.List;

@Data
public class VariantGroup {
    private String id;
    private String label;
    private List<VariantValue> values;
}
