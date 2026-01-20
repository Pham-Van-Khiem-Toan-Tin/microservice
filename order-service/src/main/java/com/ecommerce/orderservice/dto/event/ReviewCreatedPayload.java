package com.ecommerce.orderservice.dto.event;

import lombok.Data;

@Data
public class ReviewCreatedPayload {
    private String orderItemId;
    private String orderId;
    private boolean isReviewed;
}
