package com.ecommerce.catalogservice.dto.request.attribute;

import lombok.Data;

@Data
public class AttributeOptionCreate {
    private String label;
    private Integer displayOrder;
    private Boolean active;
}
