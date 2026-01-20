package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.request.CartForm;
import com.ecommerce.orderservice.dto.response.CartDTO;
import com.ecommerce.orderservice.entity.CartEntity;

public interface CartService {
    void addToCart(CartForm form);
    CartDTO getCart(String userId);
    void removeCartItem(String userId, String productId);
    void clearCart(String userId);
    void recalculateTotal(CartEntity cart);
}
