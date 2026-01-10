package com.ecommerce.catalogservice.dto.request.attribute;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.OptionEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeEditForm {
    private String id;
    private String code;
    private String label;
    private Boolean active;
    private AttributeDataType dataType;
    private String unit;
    private List<OptionEntity> options;
}
