package com.ecommerce.orderservice.dto.response.order;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderItemResponse {
    private String productName;
    private String variantName;
    private String productThumbnail;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
