package com.ecommerce.catalogservice.dto.response.category;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class FilterOptionDTO {
    private String id;
    private String label;
}
