package com.ecommerce.catalogservice.dto.request.attribute;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeCreateForm {
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private Boolean active;
    private List<AttributeOptionCreate> options;
}
