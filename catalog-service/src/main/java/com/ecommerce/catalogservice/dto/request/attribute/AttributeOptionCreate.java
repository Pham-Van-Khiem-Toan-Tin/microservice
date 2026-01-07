package com.ecommerce.catalogservice.dto.request.attribute;

import lombok.Data;

@Data
public class AttributeOptionCreate {
    private String id;
    private String label;
    private String value;
}
