package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.response.ProductListItemResponse;

import java.util.List;

public interface WishlistService {
    List<ProductListItemResponse> getWishList();
    List<String> getWishlistProductIds();
    void addProduct(String productId);
    void removeProduct(String productId);
    boolean isInWishlist(String productId);
}
