package com.ecommerce.catalogservice.dto.response.attribute;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.OptionEntity;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class AttributeDetailDTO {
    private String id;
    private String code;
    private String label;
    private AttributeDataType dataType;
    private Boolean active;
    private String unit;
    private Boolean deleted;
    private List<OptionEntity> options;
    private UsageDTO usage;
    private CapabilitiesDTO capabilities;
}
