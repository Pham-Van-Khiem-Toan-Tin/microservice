package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.constants.Constants;
import com.ecommerce.orderservice.dto.request.CartForm;
import com.ecommerce.orderservice.dto.response.ApiResponse;
import com.ecommerce.orderservice.dto.response.CartDTO;
import com.ecommerce.orderservice.service.CartService;
import com.ecommerce.orderservice.utils.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import static com.ecommerce.orderservice.constants.Constants.*;

@RestController
@RequestMapping("/cart")
public class CartController {
    @Autowired
    private CartService cartService;
    @PreAuthorize("hasAuthority('VIEW_CART')")
    @GetMapping
    public CartDTO getCart() {
        return cartService.getCart(AuthenticationUtils.getUserId());
    };
    @PreAuthorize("hasAuthority('EDIT_CART')")
    @PostMapping("/add")
    public ApiResponse<Void> updateCart(@RequestBody CartForm form) {
         cartService.addToCart(form);
         return ApiResponse.ok(ADD_CART_SUCCESS);
    }
    @PreAuthorize("hasAuthority('EDIT_CART')")
    @DeleteMapping("/remove/{skuId}")
    public ApiResponse<Void> deleteCartItem(@PathVariable String skuId) {
        cartService.removeCartItem(AuthenticationUtils.getUserId(), skuId);
        return ApiResponse.ok(REMOVE_CART_SUCCESS);
    }
}
