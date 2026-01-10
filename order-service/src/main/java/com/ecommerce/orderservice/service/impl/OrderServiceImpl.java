package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.constants.Constants;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.dto.request.UpdateOrderRequest;
import com.ecommerce.orderservice.dto.response.*;
import com.ecommerce.orderservice.entity.OrderEntity;
import com.ecommerce.orderservice.entity.OrderStatus;
import com.ecommerce.orderservice.entity.PaymentStatus;
import com.ecommerce.orderservice.repository.OrderItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.specs.OrderSpecification;
import com.ecommerce.orderservice.utils.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import static com.ecommerce.orderservice.constants.Constants.VALIDATE_FAIL;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;


    @Override
    public Page<OrderDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        List<String> safeFields = normalizeFields(fields);
        Specification<OrderEntity> spec =
                OrderSpecification.keywordLike(keyword, safeFields);
        Sort s = parseSort(sort);
        System.out.println("sort input=" + sort + ", parsed=" + s);
        Pageable pageable =
                PageRequest.of(Math.max(0, page), clampSize(size), parseSort(sort));
        return orderRepository.findAll(spec, pageable)
                .map(order -> OrderDTO
                        .builder()
                        .id(order.getId())
                        .paymentStatus(order.getPaymentStatus())
                        .finalAmount(order.getFinalAmount())
                        .orderNumber(order.getOrderNumber())
                        .orderStatus(order.getStatus())
                        .build());
    }

    private static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of("order_number", "user_id");
    private static final Set<String> ALLOWED_SORT_FIELDS =
            Set.of("order_number", "user_id", "created_at", "final_amount");
    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.DESC, "orderNumber"); // ✅ camelCase
        }

        String[] parts = sort.split(":", 2);
        String rawField = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim() : "asc";

        Sort.Direction direction =
                "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // whitelist theo INPUT từ FE (snake_case) nhưng output phải là camelCase
        String field = switch (rawField) {
            case "order_number", "orderNumber" -> "orderNumber";
            case "user_id", "userId" -> "userId";
            case "created_at", "createdAt" -> "createdAt";
            case "final_amount", "finalAmount" -> "finalAmount";
            default -> "orderNumber";
        };

        return Sort.by(direction, field);
    }

    private int clampSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, 100);
    }

    private List<String> normalizeFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) return List.of("orderNumber"); // ✅ default camelCase

        return fields.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(ALLOWED_SEARCH_FIELDS::contains)
                .map(f -> switch (f) {
                    case "order_number" -> "orderNumber";
                    case "user_id" -> "userId";
                    default -> null;
                })
                .filter(Objects::nonNull)
                .distinct()
                .toList();
    }

    @Override
    public OrderExistenceDTO findProductExist(String productId) {
        Boolean existed = orderItemRepository.existsByProductId(productId);
        return OrderExistenceDTO.builder().exists(existed).build();
    }

    @Override
    public void updateOrder(String orderId, UpdateOrderRequest req) {
        OrderEntity order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        OrderStatus current = order.getStatus();
        OrderStatus next = req.getStatus();

        if (!isValidTransition(current, next)) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        order.setStatus(next);

        // ví dụ rule: khi delivered thì paymentStatus phải paid (tuỳ bạn)
        if (next == OrderStatus.DELIVERED && order.getPaymentStatus() == PaymentStatus.UNPAID) {
            // tuỳ nghiệp vụ: auto set PAID hoặc throw
            // order.setPaymentStatus(PaymentStatus.PAID);
            throw new BusinessException(VALIDATE_FAIL);
        }

        // cancel reason (nếu bạn có field)
        if (next == OrderStatus.CANCELLED && (req.getReason() == null || req.getReason().isBlank())) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        // order.setCancelReason(req.reason()); // nếu có
        orderRepository.save(order);

    }

    @Override
    public OrderDetailDTO findOrderDetail(String orderId) {
        if (!StringUtils.hasText(orderId)) throw new BusinessException(VALIDATE_FAIL);
        OrderEntity order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        return OrderDetailDTO.builder()
                .id(order.getId())
                .orderNumber(order.getOrderNumber())
                .finalAmount(order.getFinalAmount())
                .userId(order.getUserId())
                .paymentStatus(order.getPaymentStatus())
                .note(order.getNote())
                .createdAt(order.getCreatedAt())
                .status(order.getStatus())
                .orderAddress(
                        OrderAddressDTO.builder()
                                .id(order.getShippingAddress().getId())
                                .addressDetail(order.getShippingAddress().getAddressDetail())
                                .ward(order.getShippingAddress().getWard())
                                .district(order.getShippingAddress().getDistrict())
                                .city(order.getShippingAddress().getCity())
                                .contactName(order.getShippingAddress().getContactName())
                                .phone(order.getShippingAddress().getPhone())
                                .build()
                )
                .orderItems(order.getOrderItems().stream().map(
                        o -> OrderItemDTO.builder()
                                .id(o.getId())
                                .productId(o.getProductId())
                                .quantity(o.getQuantity())
                                .unitPrice(o.getUnitPrice())
                                .skuCode(o.getSkuCode())
                                .productName(o.getProductName())
                                .productThumbnail(o.getProductThumbnail())
                                .variantName(o.getVariantName())
                                .subTotal(o.getSubTotal())
                                .build()
                ).toList())
                .paymentMethod(order.getPaymentMethod())
                .updatedAt(order.getUpdatedAt())
                .discountAmount(order.getDiscountAmount())
                .subTotalAmount(order.getSubTotalAmount())
                .build();
    }

    @Override
    public List<OrderDTO> findAllMyOrder() {
        String userId = AuthenticationUtils.getUserId();
        if (!StringUtils.hasText(userId)) throw new BusinessException(VALIDATE_FAIL);
        List<OrderEntity> orders = orderRepository.findAllByUserId(userId);
        return orders.stream().map(
                order -> OrderDTO.builder()
                        .id(order.getId())
                        .paymentStatus(order.getPaymentStatus())
                        .finalAmount(order.getFinalAmount())
                        .orderNumber(order.getOrderNumber())
                        .orderStatus(order.getStatus())
                        .build()
        ).toList();
    }

    @Override
    public OrderDetailDTO findMyOrderDetail(String id) {
        OrderEntity order = orderRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        return  OrderDetailDTO.builder()
                        .id(order.getId())
                        .orderNumber(order.getOrderNumber())
                        .finalAmount(order.getFinalAmount())
                        .userId(order.getUserId())
                        .paymentStatus(order.getPaymentStatus())
                        .note(order.getNote())
                        .createdAt(order.getCreatedAt())
                        .status(order.getStatus())
                        .orderAddress(
                                OrderAddressDTO.builder()
                                        .id(order.getShippingAddress().getId())
                                        .addressDetail(order.getShippingAddress().getAddressDetail())
                                        .ward(order.getShippingAddress().getWard())
                                        .district(order.getShippingAddress().getDistrict())
                                        .city(order.getShippingAddress().getCity())
                                        .contactName(order.getShippingAddress().getContactName())
                                        .phone(order.getShippingAddress().getPhone())
                                        .build()
                        )
                        .orderItems(order.getOrderItems().stream().map(
                                o -> OrderItemDTO.builder()
                                        .id(o.getId())
                                        .productId(o.getProductId())
                                        .quantity(o.getQuantity())
                                        .unitPrice(o.getUnitPrice())
                                        .skuCode(o.getSkuCode())
                                        .productName(o.getProductName())
                                        .productThumbnail(o.getProductThumbnail())
                                        .variantName(o.getVariantName())
                                        .subTotal(o.getSubTotal())
                                        .build()
                        ).toList())
                        .paymentMethod(order.getPaymentMethod())
                        .updatedAt(order.getUpdatedAt())
                        .discountAmount(order.getDiscountAmount())
                        .subTotalAmount(order.getSubTotalAmount())
                        .build();
    }

    private boolean isValidTransition(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) return false;
        return switch (from) {
            case PENDING -> to == OrderStatus.CONFIRMED || to == OrderStatus.CANCELLED;
            case CONFIRMED -> to == OrderStatus.SHIPPING || to == OrderStatus.CANCELLED;
            case SHIPPING -> to == OrderStatus.DELIVERED || to == OrderStatus.CANCELLED;
            case DELIVERED, CANCELLED -> false;
            case RETURNED -> false;
        };
    }
}
