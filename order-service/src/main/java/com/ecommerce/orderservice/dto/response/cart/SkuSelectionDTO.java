package com.ecommerce.orderservice.dto.response.cart;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SkuSelectionDTO {
    private String groupName;
    private String label;
}
