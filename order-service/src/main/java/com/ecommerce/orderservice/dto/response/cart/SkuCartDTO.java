package com.ecommerce.orderservice.dto.response.cart;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class SkuCartDTO {
    private String id;
    private String spuId;
    private String skuCode;
    private String spuName;
    private String skuName;
    private String thumbnail;
    private BigDecimal price;
    private List<SkuSelectionDTO> selections;
}
