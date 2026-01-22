package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryReservationRepository extends JpaRepository<ReservationEntity, UUID> {
    Optional<ReservationEntity> findByOrderId(UUID orderId);
    boolean existsByOrderId(UUID orderId);
}
