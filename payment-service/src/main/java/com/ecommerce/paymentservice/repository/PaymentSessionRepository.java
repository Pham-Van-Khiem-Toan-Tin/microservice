package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.PaymentSessionEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PaymentSessionRepository extends JpaRepository<PaymentSessionEntity, UUID> {
    Optional<PaymentSessionEntity> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);

    Optional<PaymentSessionEntity> findByOrderNumber(String orderNumber);
}
