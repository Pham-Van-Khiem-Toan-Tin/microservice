package com.ecommerce.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class WishlistItemId implements Serializable {
    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "product_id", nullable = false)
    private String productId;

    protected WishlistItemId() {}

    public WishlistItemId(String userId, String productId) {
        this.userId = userId;
        this.productId = productId;
    }

    public String getUserId() { return userId; }
    public String getProductId() { return productId; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof WishlistItemId)) return false;
        WishlistItemId that = (WishlistItemId) o;
        return Objects.equals(userId, that.userId)
                && Objects.equals(productId, that.productId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, productId);
    }
}
