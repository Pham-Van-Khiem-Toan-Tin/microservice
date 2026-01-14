package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AttributeOptionDTO {
    private Boolean deprecated;
    private boolean active;
    private String label;
    private String id;
    private boolean selected;
}
