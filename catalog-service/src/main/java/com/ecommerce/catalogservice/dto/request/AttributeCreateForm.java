package com.ecommerce.catalogservice.dto.request;

import com.ecommerce.catalogservice.entity.AttributeDataType;
import com.ecommerce.catalogservice.entity.OptionEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class AttributeCreateForm {
    private String code;
    private String label;
    private AttributeDataType dataType;
    private String unit;
    private List<OptionEntity> options;
}
