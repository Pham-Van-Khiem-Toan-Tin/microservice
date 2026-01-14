package com.ecommerce.catalogservice.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SpecOptionDTO {
    private String id;
    private String label;
}
