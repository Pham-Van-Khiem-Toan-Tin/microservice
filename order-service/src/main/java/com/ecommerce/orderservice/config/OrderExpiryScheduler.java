package com.ecommerce.orderservice.config;

import com.ecommerce.orderservice.service.OrderExpiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OrderExpiryScheduler {
    @Autowired
    private OrderExpiryService orderExpiryService;
    @Scheduled(fixedDelayString = "${order.expiry.fixedDelayMs:30000}")
    public void expireOverdueOrders() {
        orderExpiryService.expireOverdueOrders();
    }
}
