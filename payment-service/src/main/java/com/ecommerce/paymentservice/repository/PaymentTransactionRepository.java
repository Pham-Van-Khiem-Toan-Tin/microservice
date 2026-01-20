package com.ecommerce.paymentservice.repository;

import com.ecommerce.paymentservice.entity.PaymentTransactionEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransactionEntity, UUID> {
    // Kiểm tra xem ID của SePay (lưu trong cột externalTransId) đã tồn tại chưa
    boolean existsByExternalTransId(String externalTransId);

    Optional<PaymentTransactionEntity> findByReferenceIdAndStatus(String referenceId, String status);
    @Query("SELECT p FROM PaymentTransactionEntity p " +
            "WHERE REPLACE(REPLACE(p.referenceId, '-', ''), ' ', '') = :normalizedId " +
            "AND p.status = 'PENDING'")
    Optional<PaymentTransactionEntity> findByNormalizedReferenceId(@Param("normalizedId") String normalizedId);
}