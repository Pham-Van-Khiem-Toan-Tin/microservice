package com.ecommerce.catalogservice.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandOptionDTOS {
    private String id;
    private String name;
}
