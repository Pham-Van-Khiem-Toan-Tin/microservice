package com.ecommerce.catalogservice.dto.response.product;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ProductPdpSpecDTO {
    private String id;
    private String label;
    private String value;
    private String unit;
    private Integer displayOrder;
}
