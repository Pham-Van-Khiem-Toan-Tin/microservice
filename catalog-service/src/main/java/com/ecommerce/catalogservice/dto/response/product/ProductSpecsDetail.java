package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductSpecsDetail {
    private String id;
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private Integer displayOrder;
    private String code;
    private Object value;
    private String valueName;
}
