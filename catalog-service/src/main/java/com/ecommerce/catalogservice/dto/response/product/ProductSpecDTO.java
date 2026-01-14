package com.ecommerce.catalogservice.dto.response.product;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ProductSpecDTO {
    private String id;
    private String code;
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private Integer displayOrder;
    private Object value;
    private SpecOptionDTO valueSelect;
    private List<SpecOptionDTO> valueMultiSelect;
}
