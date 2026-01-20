package com.ecommerce.catalogservice.dto.response.cart;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SkuSelectionDTO {
    private String groupName;
    private String label;
}
