package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.OptionEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttributeDetailDTO {
    private String id;
    private String label;
    private String unit;
    private String code;
    private AttributeDataType dataType;
    private List<OptionEntity> options;
}
