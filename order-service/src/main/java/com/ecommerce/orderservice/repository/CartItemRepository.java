package com.ecommerce.orderservice.repository;

import com.ecommerce.orderservice.entity.CartItem;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {
    @Modifying
    @Transactional
    @Query("DELETE FROM CartItem c WHERE c.cart.userId = :userId")
    void deleteByUserId(String userId);

}
