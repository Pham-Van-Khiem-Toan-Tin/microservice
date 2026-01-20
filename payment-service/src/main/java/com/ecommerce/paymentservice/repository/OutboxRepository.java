package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
}
