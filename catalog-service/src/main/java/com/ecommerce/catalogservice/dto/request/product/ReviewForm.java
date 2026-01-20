package com.ecommerce.catalogservice.dto.request.product;

import lombok.Data;

@Data
public class ReviewForm {
    private String productId;
    private String skuId;
    private String orderId;
    private String orderItemId;
    private Integer rating;
    private String comment;
    private String skuAttributes;
}
