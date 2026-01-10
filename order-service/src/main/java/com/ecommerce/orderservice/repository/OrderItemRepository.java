package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItemEntity, UUID> {

    Boolean existsByProductId(String productId);
}
