package com.ecommerce.orderservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class CartDTO {
    private String id;
    private BigDecimal totalPrice;
    private List<CartItemDTO> items;
}
