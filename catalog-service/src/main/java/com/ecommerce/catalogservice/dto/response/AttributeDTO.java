package com.ecommerce.catalogservice.dto.response;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class AttributeDTO {
    private String id;
    private String code;
    private String label;
    private Boolean active;
    private AttributeDataType dataType;
}
