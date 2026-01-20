package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.entity.OrderEntity;

public interface OrderStatisticsService {
    void updateStatsOnNewOrder(OrderEntity order);
}
