package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderStatsDaily;
import com.ecommerce.orderservice.repository.projection.DailyProjection;
import com.ecommerce.orderservice.repository.projection.OverviewProjection;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface OrderStatsDailyRepository extends JpaRepository<OrderStatsDaily, LocalDate> {

    /**
     * Overview trong khoảng ngày [from..to] (inclusive)
     */
    @Query(value = """
        SELECT
          COALESCE(SUM(total_orders), 0) AS totalOrders,
          COALESCE(SUM(delivered_orders), 0) AS deliveredOrders,
          COALESCE(SUM(cancelled_orders), 0) AS cancelledOrders,

          COALESCE(SUM(revenue), 0) AS revenue,
          COALESCE(SUM(gross_amount), 0) AS grossAmount,
          COALESCE(SUM(discount_amount), 0) AS discountAmount,

          COALESCE(SUM(items_sold), 0) AS itemsSold,

          COALESCE(SUM(cod_orders), 0) AS codOrders,
          COALESCE(SUM(vnpay_orders), 0) AS vnpayOrders,
          COALESCE(SUM(bank_transfer_orders), 0) AS bankTransferOrders
        FROM order_stats_daily
        WHERE stat_date >= :from AND stat_date <= :to
        """, nativeQuery = true)
    OverviewProjection overview(@Param("from") LocalDate from, @Param("to") LocalDate to);

    /**
     * Trả về series theo ngày để vẽ chart
     */
    @Query(value = """
        SELECT
          stat_date AS statDate,
          total_orders AS totalOrders,
          revenue AS revenue
        FROM order_stats_daily
        WHERE stat_date >= :from AND stat_date <= :to
        ORDER BY stat_date
        """, nativeQuery = true)
    List<DailyProjection> daily(@Param("from") LocalDate from, @Param("to") LocalDate to);

    @Modifying
    @Transactional
    @Query(value = """
        INSERT INTO order_stats_daily (
            stat_date, total_orders, delivered_orders, cancelled_orders, 
            revenue, gross_amount, discount_amount, items_sold, 
            cod_orders, vnpay_orders, bank_transfer_orders
        ) VALUES (
            :#{#s.statDate}, :#{#s.totalOrders}, :#{#s.deliveredOrders}, :#{#s.cancelledOrders}, 
            :#{#s.revenue}, :#{#s.grossAmount}, :#{#s.discountAmount}, :#{#s.itemsSold}, 
            :#{#s.codOrders}, :#{#s.vnpayOrders}, :#{#s.bankTransferOrders}
        ) ON DUPLICATE KEY UPDATE 
            total_orders = total_orders + VALUES(total_orders),
            revenue = revenue + VALUES(revenue),
            gross_amount = gross_amount + VALUES(gross_amount),
            discount_amount = discount_amount + VALUES(discount_amount),
            items_sold = items_sold + VALUES(items_sold),
            cod_orders = cod_orders + VALUES(cod_orders),
            vnpay_orders = vnpay_orders + VALUES(vnpay_orders),
            bank_transfer_orders = bank_transfer_orders + VALUES(bank_transfer_orders),
            delivered_orders = delivered_orders + VALUES(delivered_orders),
            cancelled_orders = cancelled_orders + VALUES(cancelled_orders)
    """, nativeQuery = true)
    void upsertStats(OrderStatsDaily s);
}
