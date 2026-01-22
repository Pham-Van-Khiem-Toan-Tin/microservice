package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.OrderEntity;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<OrderEntity, UUID>, JpaSpecificationExecutor<OrderEntity> {
    @Override
    @EntityGraph(attributePaths = {"orderItems", "shippingAddress"})
    @NonNull
    Page<OrderEntity> findAll(Specification<OrderEntity> spec, Pageable  pageable);

    List<OrderEntity> findAllByUserId(String userId);

    Page<OrderEntity> findByUserId(String userId, Pageable pageable);

    List<OrderEntity> findAllByUserIdAndId(String userId, UUID id);

    List<OrderEntity> findByUserIdAndId(String userId, UUID id);

    Optional<OrderEntity> findByOrderNumber(String orderNumber);
    @Query(value = """
        SELECT * FROM t_orders
        WHERE status IN ('PENDING', 'RESERVED')
        AND expires_at <= :now
        ORDER BY expires_at ASC
        LIMIT :limit
        FOR UPDATE SKIP LOCKED
    """, nativeQuery = true)
    List<OrderEntity> findOverdueForUpdateSkipLocked(@Param("now") LocalDateTime now,
                                               @Param("limit") int limit);
}
