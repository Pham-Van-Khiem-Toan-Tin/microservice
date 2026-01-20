package com.ecommerce.identityservice.reppository;


import com.ecommerce.identityservice.entity.WishlistItemEntity;
import com.ecommerce.identityservice.entity.WishlistItemId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WishlistItemRepository extends JpaRepository<WishlistItemEntity, WishlistItemId> {
    List<WishlistItemEntity> findByIdUserIdOrderByCreatedAtDesc(String userId);
    boolean existsByIdUserIdAndIdProductId(String userId, String productId);
    void deleteByIdUserIdAndIdProductId(String userId, String productId);

    long countByIdUserId(String idUserId);

    List<WishlistItemEntity> findByIdUserId(String idUserId);
}
