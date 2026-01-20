package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.response.BusinessException;
import com.ecommerce.identityservice.dto.response.ProductListItemResponse;
import com.ecommerce.identityservice.entity.WishlistItemEntity;
import com.ecommerce.identityservice.integration.SearchClient;
import com.ecommerce.identityservice.reppository.WishlistItemRepository;
import com.ecommerce.identityservice.service.WishlistService;
import com.ecommerce.identityservice.utils.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ecommerce.identityservice.constants.Constants.WISH_LIST_PRODUCT_EXIST;

@Service
@Transactional(readOnly=true)
@Slf4j
public class WishlistServiceImpl implements WishlistService {
    @Autowired
    private WishlistItemRepository wishlistRepository;
    @Autowired
    private SearchClient searchClient;
    @Override
    public List<ProductListItemResponse> getWishList() {
        String userId = AuthenticationUtils.getUserId();
        List<WishlistItemEntity> wishlists = wishlistRepository.findByIdUserId(userId);
        List<String> productIds = wishlists.stream().map(WishlistItemEntity::getProductId).toList();
        if (productIds.isEmpty()) return List.of();
        try {
            return searchClient.getProductsByIds(productIds);
        } catch (Exception e) {
            log.error(e.getMessage());
            return List.of();
        }
    }

    @Override
    public List<String> getWishlistProductIds() {
        return wishlistRepository.findByIdUserIdOrderByCreatedAtDesc(AuthenticationUtils.getUserId())
                .stream()
                .map(WishlistItemEntity::getProductId)
                .toList();
    }
    @Transactional
    @Override
    public void addProduct(String productId) {
        String userId = AuthenticationUtils.getUserId();
        if (wishlistRepository.existsByIdUserIdAndIdProductId(userId, productId)) {
            throw new BusinessException(WISH_LIST_PRODUCT_EXIST);
        }
        wishlistRepository.save(new WishlistItemEntity(userId, productId));
    }
    @Transactional
    @Override
    public void removeProduct(String productId) {
        wishlistRepository.deleteByIdUserIdAndIdProductId(AuthenticationUtils.getUserId(), productId);
    }

    @Override
    public boolean isInWishlist(String productId) {
        return wishlistRepository.existsByIdUserIdAndIdProductId(AuthenticationUtils.getUserId(), productId);
    }
}
