package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.InventoryHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface InventoryHistoryRepository extends JpaRepository<InventoryHistoryEntity, UUID> {
}
