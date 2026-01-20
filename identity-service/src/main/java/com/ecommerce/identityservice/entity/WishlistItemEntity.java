package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
@Table(
        name = "wishlist_items",
        indexes = {
                @Index(name = "idx_wishlist_user", columnList = "user_id"),
                @Index(name = "idx_wishlist_product", columnList = "product_id")
        }
)
public class WishlistItemEntity {
    @EmbeddedId
    private WishlistItemId id;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected WishlistItemEntity() {}

    public WishlistItemEntity(String userId, String productId) {
        this.id = new WishlistItemId(userId, productId);
    }



    @PrePersist
    void prePersist() {
        if (createdAt == null) createdAt = Instant.now();
    }

    public WishlistItemId getId() { return id; }
    public String getUserId() { return id.getUserId(); }
    public String getProductId() { return id.getProductId(); }
    public Instant getCreatedAt() { return createdAt; }
}
