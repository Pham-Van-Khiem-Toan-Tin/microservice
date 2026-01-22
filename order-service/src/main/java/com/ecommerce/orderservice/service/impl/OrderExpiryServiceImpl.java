package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.dto.response.order.OrderResponse;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.OutboxEvent;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.OrderExpiryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class OrderExpiryServiceImpl implements OrderExpiryService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Transactional
    @Override
    public void expireOverdueOrders() {
        int batchSize = 200;

        List<OrderEntity> orders = orderRepository.findOverdueForUpdateSkipLocked(
                LocalDateTime.now(), batchSize
        );

        for (OrderEntity o : orders) {
            if (o.getStatus() != OrderStatus.RESERVED) continue;

            o.setStatus(OrderStatus.EXPIRED);
            o.setUpdatedAt(LocalDateTime.now());

            OutboxEvent event = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("order")
                    .aggregateId(o.getId().toString())
                    .type("ORDER_EXPIRED")
                    .payload("""
                        {"orderId":"%s","reason":"TIMEOUT","expiredAt":"%s"}
                    """.formatted(o.getId(), LocalDateTime.now()))
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(event);
        }

        orderRepository.saveAll(orders);
    }
}
