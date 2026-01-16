package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.InventoryHistoryEntity;
import com.ecommerce.inventoryservice.entity.InventoryType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistoryEntity, UUID> {
    Page<InventoryHistoryEntity> findBySkuCode(String skuCode, Pageable pageable);

    Page<InventoryHistoryEntity> findBySkuCodeAndType(String skuCode, InventoryType type, Pageable pageable);

    Page<InventoryHistoryEntity> findBySkuCodeAndCreatedAtBetween(String skuCode, Instant from, Instant to, Pageable pageable);

    Page<InventoryHistoryEntity> findBySkuCodeAndTypeAndCreatedAtBetween(
            String skuCode, InventoryType type, Instant from, Instant to, Pageable pageable
    );
}
