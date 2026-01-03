package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;

@Data
@Builder
public class AttributeConfigDTO {
    private String id;
    private String code;
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private boolean isRequired;
    private boolean isFilterable;
    private Integer displayOrder;
    private List<AttributeOptionDTO> optionsValue;
}
