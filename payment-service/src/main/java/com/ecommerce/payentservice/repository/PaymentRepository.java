package com.ecommerce.payentservice.repository;

import com.ecommerce.payentservice.entity.BillingEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PaymentRepository extends JpaRepository<BillingEntity, Long> {
    BillingEntity findByCustomerId(String customerId);
}
