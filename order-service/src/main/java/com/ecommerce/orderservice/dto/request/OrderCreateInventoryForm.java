package com.ecommerce.orderservice.dto.request;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class OrderCreateInventoryForm {
    private String orderId;
    private String orderNumber;
    private List<OrderItemCheckForm> items;
}
