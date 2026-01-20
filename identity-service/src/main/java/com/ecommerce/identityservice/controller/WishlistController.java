package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.dto.response.ProductListItemResponse;
import com.ecommerce.identityservice.service.WishlistService;
import com.ecommerce.identityservice.utils.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/wishlist")
public class WishlistController {
    @Autowired
    private WishlistService wishlistService;
    @GetMapping
    public List<ProductListItemResponse> getWishlist() {
        return wishlistService.getWishList();
    }
    @PostMapping("/{productId}")
    public ApiResponse<Void> add(@PathVariable String productId) {
        wishlistService.addProduct(productId);
        return ApiResponse.ok(Constants.WISH_LIST_UPDATE_SUCCESS);
    }

    @DeleteMapping("/{productId}")
    public ApiResponse<Void> remove(@PathVariable String productId) {
        wishlistService.removeProduct(productId);
        return ApiResponse.ok(Constants.WISH_LIST_UPDATE_SUCCESS);
    }
}
