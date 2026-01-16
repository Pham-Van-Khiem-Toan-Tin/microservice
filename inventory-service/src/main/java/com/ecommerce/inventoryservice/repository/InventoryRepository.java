package com.ecommerce.inventoryservice.repository;

import com.ecommerce.inventoryservice.entity.InventoryEntity;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface InventoryRepository extends JpaRepository<InventoryEntity, UUID> {
    Page<InventoryEntity> findBySkuCodeContainingIgnoreCase(String skuCode, Pageable pageable);
    boolean existsBySkuCode(String skuCode);

    // IMPORT: total += qty
    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.totalStock = i.totalStock + :qty
        WHERE i.skuCode = :skuCode
    """)
    int incTotal(@Param("skuCode") String skuCode, @Param("qty") int qty);

    // EXPORT: total -= qty, nhưng phải đảm bảo available >= qty
    // available = total - reserved
    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.totalStock = i.totalStock - :qty
        WHERE i.skuCode = :skuCode
          AND (i.totalStock - i.reservedStock) >= :qty
    """)
    int decTotalIfAvailable(@Param("skuCode") String skuCode, @Param("qty") int qty);

    // ADJUST: set total = newTotal, nhưng phải đảm bảo newTotal >= reserved
    @Modifying
    @Query("""
        UPDATE InventoryEntity i
        SET i.totalStock = :newTotal
        WHERE i.skuCode = :skuCode
          AND :newTotal >= i.reservedStock
    """)
    int setTotalIfNotBelowReserved(@Param("skuCode") String skuCode, @Param("newTotal") int newTotal);
    // 1. Logic Giữ hàng (Reservation)
    // Trả về 1 nếu update thành công, 0 nếu không thỏa điều kiện (Hết hàng)
    @Transactional
    @Modifying
    @Query("UPDATE InventoryEntity i SET i.reservedStock = i.reservedStock + :qty " +
            "WHERE i.skuCode = :skuCode " +
            "AND (i.totalStock - i.reservedStock) >= :qty")
    int reserveStock(String skuCode, Integer qty);

    // 2. Logic Xác nhận bán (Trừ kho thật)
    @Transactional
    @Modifying
    @Query("UPDATE InventoryEntity i SET i.reservedStock = i.reservedStock - :qty, " +
            "i.totalStock = i.totalStock - :qty " +
            "WHERE i.skuCode = :skuCode")
    void confirmStock(String skuCode, Integer qty);

    // 3. Logic Hủy giữ hàng (Hoàn lại)
    @Transactional
    @Modifying
    @Query("UPDATE InventoryEntity i SET i.reservedStock = i.reservedStock - :qty " +
            "WHERE i.skuCode = :skuCode")
    void cancelReservation(String skuCode, Integer qty);

    Optional<InventoryEntity> findBySkuCode(String skuCode);
}
