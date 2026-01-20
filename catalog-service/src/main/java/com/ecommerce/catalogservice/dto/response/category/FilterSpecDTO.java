package com.ecommerce.catalogservice.dto.response.category;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class FilterSpecDTO {
    private String code;
    private String label;
    private AttributeDataType dataType;
    private Integer displayOrder;
    private List<FilterOptionDTO> options;
}
