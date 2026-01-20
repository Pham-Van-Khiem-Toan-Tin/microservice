package com.ecommerce.orderservice.dto.response;

import com.ecommerce.orderservice.dto.response.cart.SkuSelectionDTO;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CartItemDTO {
    private String skuId;
    private String skuCode;
    private String productName;
    private String productId;
    private String skuName;
    private String thumbnail;
    private BigDecimal price;
    private int quantity;
    private int stock;
    private BigDecimal subTotal;
    private List<SkuSelectionDTO> options;
    boolean isOutOfStock;
    boolean isPriceChanged;
}
