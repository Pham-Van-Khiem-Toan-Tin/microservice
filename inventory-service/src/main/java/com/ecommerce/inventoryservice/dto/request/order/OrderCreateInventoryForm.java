package com.ecommerce.inventoryservice.dto.request.order;

import lombok.Data;

import java.util.List;

@Data
public class OrderCreateInventoryForm {
    private String orderId;
    private String orderNumber;
    private List<OrderItemCheckForm> items;
}
