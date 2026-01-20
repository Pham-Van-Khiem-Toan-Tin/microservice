package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderProductStatsDaily;
import com.ecommerce.orderservice.entity.OrderProductStatsDailyId;
import com.ecommerce.orderservice.repository.projection.TopProductProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderProductStatsDailyRepository extends JpaRepository<OrderProductStatsDaily, OrderProductStatsDailyId> {

    /**
     * Top products trong khoảng ngày [from..to] (inclusive)
     * Gộp từ bảng daily product stats.
     */
    @Query(value = """
        SELECT
          product_id AS productId,
          MAX(product_name) AS productName,
          COALESCE(SUM(quantity), 0) AS quantity,
          COALESCE(SUM(revenue), 0) AS revenue
        FROM order_product_stats_daily
        WHERE stat_date >= :from AND stat_date <= :to
        GROUP BY product_id
        ORDER BY quantity DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<TopProductProjection> topProducts(@Param("from") LocalDate from,
                                           @Param("to") LocalDate to,
                                           @Param("limit") int limit);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO order_product_stats_daily (
            stat_date, product_id, product_name, quantity, revenue
        ) VALUES (
            :statDate, :productId, :productName, :quantity, :revenue
        ) ON DUPLICATE KEY UPDATE 
            quantity = quantity + VALUES(quantity),
            revenue = revenue + VALUES(revenue),
            product_name = VALUES(product_name)
    """, nativeQuery = true)
    void upsertProductStats(
            @Param("statDate") LocalDate statDate,
            @Param("productId") String productId,
            @Param("productName") String productName,
            @Param("quantity") Long quantity,
            @Param("revenue") BigDecimal revenue
    );
}
