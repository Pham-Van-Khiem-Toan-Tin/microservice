package com.ecommerce.catalogservice.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BrandPdpDTO {
    private String id;
    private String name;
    private String slug;
}
