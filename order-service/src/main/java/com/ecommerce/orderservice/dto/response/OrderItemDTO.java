package com.ecommerce.orderservice.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
@Builder
public class OrderItemDTO {
    private UUID id;
    private String skuCode;
    private String productId;
    private String productName;
    private String variantName;
    private String productThumbnail;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subTotal;
}
