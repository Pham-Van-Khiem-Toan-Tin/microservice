package com.ecommerce.orderservice.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartForm {
    private String skuId;
    private int quantity;
}
