package com.ecommerce.catalogservice.dto.response.attribute;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UsageDTO {
    private Boolean usedInCategories;
    private Boolean usedInProducts;
    private long categoryCount;
    private long productCount;
}
