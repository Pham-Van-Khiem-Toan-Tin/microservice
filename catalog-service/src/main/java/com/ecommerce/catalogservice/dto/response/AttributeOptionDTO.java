package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributeOptionDTO {
    private boolean active;
    private String value;
    private String label;
}
