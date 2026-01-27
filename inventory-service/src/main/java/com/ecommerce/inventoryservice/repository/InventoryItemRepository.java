package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.InventoryItemEntity;
import com.ecommerce.inventoryservice.entity.InventoryItemStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItemEntity, UUID> {
    List<InventoryItemEntity> findTopBySkuCodeAndStatus(String skuCode, InventoryItemStatus status, Pageable pageable);

    List<InventoryItemEntity> findAllByOrderNumber(String orderNumber);
    @Query("SELECT i FROM InventoryItemEntity i " +
            "WHERE i.skuCode IN :skuCodes " +
            "AND i.status = 'AVAILABLE' " +
            "ORDER BY i.skuCode ASC, (CASE WHEN i.orderNumber = :orderNo THEN 0 ELSE 1 END) ASC")
    List<InventoryItemEntity> findAllPotentialSerials(
            @Param("skuCodes") List<String> skuCodes,
            @Param("orderNo") String orderNo
    );

    List<InventoryItemEntity> findAllByStatusAndReservedAtBefore(InventoryItemStatus status, LocalDateTime reservedAtBefore);

    boolean existsBySerialNumberIn(Collection<String> serialNumbers);
    List<InventoryItemEntity> findBySerialNumberInAndStatus(Collection<String> serialNumbers, InventoryItemStatus status);
}
