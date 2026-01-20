package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.constants.Constants;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.dto.request.CartForm;
import com.ecommerce.orderservice.dto.response.CartDTO;
import com.ecommerce.orderservice.dto.response.CartItemDTO;
import com.ecommerce.orderservice.dto.response.cart.SkuCartDTO;
import com.ecommerce.orderservice.dto.response.inventory.InventoryDto;
import com.ecommerce.orderservice.entity.CartEntity;
import com.ecommerce.orderservice.entity.CartItem;
import com.ecommerce.orderservice.integration.CatalogFeignClient;
import com.ecommerce.orderservice.integration.InventoryFeignClient;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.service.CartService;
import com.ecommerce.orderservice.utils.AuthenticationUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.ecommerce.orderservice.constants.Constants.VALIDATE_FAIL;

@Service
@Slf4j
public class CartServiceImpl implements CartService {
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private CatalogFeignClient catalogFeignClient;
    @Autowired
    private InventoryFeignClient inventoryFeignClient;

    @Transactional
    @Override
    public void addToCart(CartForm form) {
        String userId = AuthenticationUtils.getUserId();
        List<SkuCartDTO> productInfos = catalogFeignClient.getSkuDetailsBatch(List.of(form.getSkuId()));

        if (productInfos.isEmpty()) {
            throw new RuntimeException("Sản phẩm không tồn tại hoặc đã bị xóa!");
        }
        SkuCartDTO productInfo = productInfos.get(0);

        // 2. Lấy giỏ hàng của user, nếu chưa có thì tạo mới
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));

        // 3. Tìm xem sản phẩm này đã có trong giỏ chưa
        Optional<CartItem> existingItemOpt = cart.getItems().stream()
                .filter(item -> item.getSkuId().equals(form.getSkuId()))
                .findFirst();

        if (existingItemOpt.isPresent()) {
            // TRƯỜNG HỢP A: Sản phẩm đã có -> Cập nhật số lượng (Cộng hoặc Trừ)
            CartItem item = existingItemOpt.get();
            int newQuantity = item.getQuantity() + form.getQuantity();

            if (newQuantity <= 0) {
                // Nếu giảm về 0 hoặc âm -> Xóa luôn khỏi giỏ
                cart.getItems().remove(item);
            } else {
                // Cập nhật số lượng mới và giá mới nhất
                item.setQuantity(newQuantity);
                item.setPrice(productInfo.getPrice());
            }
        } else {
            // TRƯỜNG HỢP B: Sản phẩm chưa có -> Thêm mới
            if (form.getQuantity() <= 0) {
                throw new RuntimeException("Số lượng thêm mới phải lớn hơn 0");
            }

            CartItem newItem = CartItem.builder()
                    .cart(cart) // Link ngược lại cha để JPA hiểu quan hệ
                    .skuId(form.getSkuId())
                    .price(productInfo.getPrice())             // Snapshot giá
                    .quantity(form.getQuantity())
                    .build();

            cart.getItems().add(newItem);
        }

        // 4. Tính lại Tổng tiền của cả giỏ hàng (Quan trọng!)
        BigDecimal totalCartPrice = cart.getItems().stream()
                .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        cart.setTotalPrice(totalCartPrice);

        // 5. Lưu xuống DB
        cartRepository.save(cart);
    }

    @Transactional
    @Override
    public CartDTO getCart(String userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseGet(() -> createNewCart(userId));
        if (cart.getItems().isEmpty()) {
            return CartDTO.builder()
                    .id(cart.getId().toString())
                    .items(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }
        List<String> skuIds = cart.getItems().stream()
                .map(CartItem::getSkuId)
                .toList();
        List<SkuCartDTO> catalogInfos = new ArrayList<>();
        try {
            catalogInfos = catalogFeignClient.getSkuDetailsBatch(skuIds);
        } catch (Exception e) {
            log.error(e.getMessage());
            return CartDTO.builder()
                    .id(cart.getId().toString())
                    .items(new ArrayList<>())
                    .totalPrice(BigDecimal.ZERO)
                    .build();
        }
        Map<String, SkuCartDTO> catalogMap = catalogInfos.stream()
                .collect(Collectors.toMap(SkuCartDTO::getId, Function.identity(), (k1, k2) -> k1));
        List<InventoryDto> inventoryInfos = new ArrayList<>();
        List<String> skuCodes = catalogInfos.stream()
                .map(SkuCartDTO::getSkuCode) // Lấy Code ra
                .filter(Objects::nonNull)
                .toList();
        try {
            inventoryInfos = inventoryFeignClient.getStockBySkuIds(skuCodes);
        } catch (Exception e) {
            log.error(e.getMessage());
            // Log lỗi Inventory
        }
        Map<String, Integer> stockMap = inventoryInfos.stream()
                .collect(Collectors.toMap(InventoryDto::getSkuCode, InventoryDto::getQuantity, (v1, v2) -> v1));

        // 5. Merge dữ liệu (Mapping)
        List<CartItemDTO> itemDtos = new ArrayList<>();
        BigDecimal totalCartPrice = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            SkuCartDTO catalogInfo = catalogMap.get(item.getSkuId());

            // Nếu sản phẩm không tìm thấy bên Catalog (đã bị xóa), ta có thể skip hoặc đánh dấu lỗi
            if (catalogInfo == null) {
                // TODO: Xử lý item lỗi (tự động xóa hoặc báo user)
                continue;
            }

            // Lấy tồn kho (default 0 nếu không tìm thấy)
            int currentStock = stockMap.getOrDefault(catalogMap.get(item.getSkuId()).getSkuCode(), 0);

            // Tính toán
            BigDecimal currentPrice = catalogInfo.getPrice();
            BigDecimal subTotal = currentPrice.multiply(BigDecimal.valueOf(item.getQuantity()));
            totalCartPrice = totalCartPrice.add(subTotal);

            // Kiểm tra tồn kho
            boolean isOutOfStock = item.getQuantity() > currentStock;

            // Kiểm tra giá có đổi không (Optional)
            boolean isPriceChanged = item.getPrice().compareTo(currentPrice) != 0;

            // Update giá mới nhất vào DB (để lần sau đồng bộ)
            if (isPriceChanged) {
                item.setPrice(currentPrice);
            }

            itemDtos.add(CartItemDTO.builder()
                    .skuId(item.getSkuId())
                    .skuCode(catalogInfo.getSkuCode())
                    .productName(catalogInfo.getSpuName())
                    .skuName(catalogInfo.getSkuName())
                    .thumbnail(catalogInfo.getThumbnail())
                    .price(currentPrice)
                    .quantity(item.getQuantity())
                    .stock(currentStock)
                    .subTotal(subTotal)
                    .options(catalogInfo.getSelections())
                    .isOutOfStock(isOutOfStock)
                    .isPriceChanged(isPriceChanged)
                    .build()
            );
        }

        // Lưu lại nếu có thay đổi giá (Sync)
        cart.setTotalPrice(totalCartPrice);
        cartRepository.save(cart);

        return new CartDTO(cart.getId().toString(), totalCartPrice, itemDtos);
    }

    @Override
    public void removeCartItem(String userId, String skuId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        boolean removed = cart.getItems().removeIf(item -> item.getSkuId().equals(skuId));

        if (removed) {
            cartRepository.save(cart);
        }
    }

    @Override
    public void clearCart(String userId) {

    }

    @Override
    public void recalculateTotal(CartEntity cart) {

    }

    private CartEntity createNewCart(String userId) {
        return cartRepository.save(CartEntity.builder()
                .userId(userId)
                .items(new ArrayList<>())
                .totalPrice(BigDecimal.ZERO)
                .build());
    }
}
