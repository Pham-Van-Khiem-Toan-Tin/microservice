package com.ecommerce.orderservice.service.impl;

import com.ecommerce.orderservice.dto.event.StockUpdatePayload;
import com.ecommerce.orderservice.dto.exception.BusinessException;
import com.ecommerce.orderservice.dto.request.*;
import com.ecommerce.orderservice.dto.response.*;
import com.ecommerce.orderservice.dto.response.cart.SkuCartDTO;
import com.ecommerce.orderservice.dto.response.inventory.InventoryAvailableDto;
import com.ecommerce.orderservice.dto.response.order.*;
import com.ecommerce.orderservice.dto.response.payment.PaymentResponse;
import com.ecommerce.orderservice.dto.response.payment.SePayResponsive;
import com.ecommerce.orderservice.entity.*;
import com.ecommerce.orderservice.integration.CatalogFeignClient;
import com.ecommerce.orderservice.integration.IdentityFeignClient;
import com.ecommerce.orderservice.integration.InventoryFeignClient;
import com.ecommerce.orderservice.integration.PaymentFeignClient;
import com.ecommerce.orderservice.repository.CartRepository;
import com.ecommerce.orderservice.repository.OrderItemRepository;
import com.ecommerce.orderservice.repository.OrderRepository;
import com.ecommerce.orderservice.repository.OutboxRepository;
import com.ecommerce.orderservice.service.OrderService;
import com.ecommerce.orderservice.service.OrderStatisticsService;
import com.ecommerce.orderservice.specs.OrderSpecification;
import com.ecommerce.orderservice.utils.AuthenticationUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ecommerce.orderservice.constants.Constants.*;
import static com.ecommerce.orderservice.entity.OrderStatus.*;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private OrderItemRepository orderItemRepository;
    @Autowired
    private CartRepository cartRepository;
    @Autowired
    private InventoryFeignClient inventoryFeignClient;
    @Autowired
    private CatalogFeignClient catalogFeignClient;
    @Autowired
    private IdentityFeignClient identityFeignClient;
    @Autowired
    private PaymentFeignClient paymentFeignClient;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private OrderStatisticsService orderStatisticsService;

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

    @Transactional
    @Override
    public void updateOrder(String orderId, UpdateOrderRequest req) {
        OrderEntity order = orderRepository.findById(UUID.fromString(orderId))
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        OrderStatus from = order.getStatus();
        OrderStatus to = req.getStatus();
        if (to == null) throw new BusinessException(VALIDATE_FAIL);

        if (!isValidTransitionForAdmin(from, to)) {
            throw new BusinessException(VALIDATE_FAIL);
        }

        // Require reason for sensitive admin operations
        if (to == OrderStatus.CANCELLED || to == OrderStatus.EXPIRED || to == OrderStatus.RETURNED) {
            if (req.getReason() == null || req.getReason().isBlank()) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        // Apply status
        order.setStatus(to);
        if (to == OrderStatus.DELIVERED) {
            if (order.getPaymentMethod() == PaymentMethod.COD
                    && order.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                order.setPaymentStatus(PaymentStatus.SUCCEEDED);
                emitStockEvent(order, "Inventory.CommitRequested");


            }
        }


        if (to == OrderStatus.PAID) {
            if (order.getPaymentStatus() != PaymentStatus.SUCCEEDED) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        if (to == OrderStatus.CANCELLED) {
            order.setCancelReason(req.getReason());

            emitStockEvent(order, "Inventory.ReleaseRequested");
        }

        if (to == OrderStatus.CONFIRMED) {
            // This is the right place to commit/deduct stock (most common design)
            emitStockEvent(order, "Inventory.CommitRequested");
        }

        // DELIVERED normally should NOT trigger stock deduction (already done at CONFIRMED)
        // RETURNED might trigger restock event:
        if (to == OrderStatus.RETURNED) {
            emitStockEvent(order, "Inventory.RestockRequested");
        }

        orderRepository.save(order);
    }

    private void emitStockEvent(OrderEntity order, String action) {
        try {
            // 1. Build Payload dùng Class (Giống style phần tạo đơn của bạn)
            StockUpdatePayload payload = StockUpdatePayload.builder()
                    .orderId(order.getId().toString())
                    .orderNo(order.getOrderNumber())
                    .action(action)
                    .items(order.getOrderItems().stream()
                            .map(item -> new StockUpdatePayload.StockItem(item.getSkuCode(), item.getQuantity()))
                            .toList())
                    .build();

            // 2. Chuyển Payload thành chuỗi JSON
            String payloadJson = objectMapper.writeValueAsString(payload);

            // 3. Tạo Entity Outbox (Khớp với cấu hình Debezium của bạn)
            OutboxEvent outbox = OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("order")
                    .aggregateId(order.getId().toString())
                    .type("ORDER_STOCK_ADJUSTMENT")
                    .payload(payloadJson)
                    .createdAt(LocalDateTime.now())
                    .build();

            outboxRepository.save(outbox);
            log.info("Successfully emitted stock event {} for order {}", action, order.getOrderNumber());

        } catch (Exception e) {
            log.error("Failed to emit stock outbox event: {}", e.getMessage());
            throw new RuntimeException("Event publishing failed");
        }
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
                                .city(order.getShippingAddress().getProvince())
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
                                .city(order.getShippingAddress().getProvince())
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
    @Transactional(readOnly = true)
    public OrderDetailsResponse getOrderByNumber(String orderNumber) {
        String userId = AuthenticationUtils.getUserId();

        // Tìm đơn hàng và kiểm tra quyền sở hữu
        OrderEntity order = orderRepository.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new BusinessException(ORDER_NOT_FOUND));

        if (!order.getUserId().equals(userId)) {
            throw new BusinessException(ACCESS_DENIED);
        }
        List<OrderDetailsResponse.OrderItemResponse> items = order.getOrderItems().stream().map(i ->
                OrderDetailsResponse.OrderItemResponse.builder()
                        .orderItemId(i.getId().toString())
                        .productId(i.getProductId())
                        .skuCode(i.getSkuCode())
                        .skuId(i.getSkuId())
                        .productName(i.getProductName())
                        .unitPrice(i.getUnitPrice())
                        .quantity(i.getQuantity())
                        .thumbnail(i.getProductThumbnail())
                        .variantName(i.getVariantName())
                        .reviewed(i.isReviewed())
                        .build()).toList();
        // Map sang DTO để trả về cho Frontend
        return OrderDetailsResponse.builder()
                .orderId(order.getId().toString())
                .orderNumber(order.getOrderNumber())
                .finalAmount(order.getFinalAmount())
                .status(order.getStatus().name()) // PENDING, PAID, v.v.
                .paymentStatus(order.getPaymentStatus())
                .phoneNumber(order.getShippingAddress().getPhone())
                .receiverName(order.getShippingAddress().getContactName())
                .phoneNumber(order.getShippingAddress().getAddressDetail())
                .items(items)
                .paymentMethod(order.getPaymentMethod())
                .createdAt(order.getCreatedAt())
                .build();
    }

    @Override
    public Page<OrderCustomerResponse> getMyOrders(Pageable pageable) {
        String userId = AuthenticationUtils.getUserId();
        Page<OrderEntity> orders = orderRepository.findByUserId(userId, pageable);

        return orders.map(this::mapToOrderResponse);
    }

    private OrderCustomerResponse mapToOrderResponse(OrderEntity entity) {
        List<OrderItemResponse> itemDtos = entity.getOrderItems().stream()
                .map(item -> OrderItemResponse.builder()
                        .productName(item.getProductName())
                        .variantName(item.getVariantName())
                        .productThumbnail(item.getProductThumbnail())
                        .quantity(item.getQuantity())
                        .unitPrice(item.getUnitPrice())
                        .subTotal(item.getSubTotal())
                        .build())
                .toList();

        return OrderCustomerResponse.builder()
                .orderNumber(entity.getOrderNumber())
                .finalAmount(entity.getFinalAmount())
                .status(entity.getStatus().name())
                .paymentStatus(entity.getPaymentStatus().name())
                .paymentMethod(entity.getPaymentMethod().name())
                .createdAt(entity.getCreatedAt())
                .items(itemDtos)
                .totalItemsCount(itemDtos.size())
                .build();
    }

    @Override
    public OrderResponse createOrder(HttpServletRequest req, OrderCreateForm form) {
        String orderNumber = "ORD-" + System.currentTimeMillis();
        try {
            MDC.put("orderNo", orderNumber);
            MDC.put("sagaStep", "ORDER_CREATE_INIT");
            MDC.put("status", "STARTED");
            log.info("Bắt đầu khởi tạo đơn hàng cho người dùng");
            String userId = AuthenticationUtils.getUserId();
            CartEntity cart = cartRepository.findByUserId(userId)
                    .orElseThrow(() -> new BusinessException(CART_INVALID));
            if (cart.getItems().isEmpty()) {
                throw new BusinessException(CART_IS_EMPTY);
            }
            List<String> skuIds = cart.getItems().stream().map(CartItem::getSkuId).toList();
            MDC.put("sagaStep", "FETCH_EXTERNAL_DATA");
            log.info("Đang lấy thông tin sản phẩm và địa chỉ từ Catalog/Identity Service");
            List<SkuCartDTO> skusInfo = catalogFeignClient.getSkuDetailsBatch(skuIds);
            Map<String, SkuCartDTO> skusInfoMap = skusInfo.stream().collect(Collectors.toMap(SkuCartDTO::getId, sku -> sku));

            BigDecimal subTotal = cart.getItems().stream()
                    .map(item -> item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal shippingFee = BigDecimal.ZERO; // Ví dụ: freeship
            BigDecimal tax = subTotal.multiply(new BigDecimal("0.08")); // Thuế 8%
            BigDecimal finalAmount = subTotal.add(tax).add(shippingFee);
            AddressDTO addressDTO = identityFeignClient.getAddressById(form.getAddressId());
            OrderAddressEntity orderAddressEntity = OrderAddressEntity.builder()
                    .contactName(addressDTO.getReceiverName())
                    .phone(addressDTO.getPhone())
                    .addressDetail(addressDTO.getDetailAddress())
                    .province(addressDTO.getProvinceName())
                    .district(addressDTO.getDistrictName())
                    .ward(addressDTO.getWardName())
                    .build();

            OrderEntity order = OrderEntity.builder()
                    .orderNumber(orderNumber)
                    .userId(cart.getUserId())
                    .subTotalAmount(subTotal)
                    .shippingFee(shippingFee)
                    .clientIp(req.getRemoteAddr())
                    .finalAmount(finalAmount)
                    .shippingAddress(orderAddressEntity)
                    .paymentMethod(form.getPaymentMethod())
                    .status(CREATED)
                    .paymentStatus(PaymentStatus.INIT)
                    .note(form.getNote())
                    .build();
            if (!form.getPaymentMethod().equals(PaymentMethod.COD)) {
                order.setExpiresAt(LocalDateTime.now().plusMinutes(15));
            }
            orderAddressEntity.setOrder(order);
            List<OrderItemEntity> orderItems = cart.getItems().stream().map(cartItem ->
                    {
                        BigDecimal itemSubTotal = cartItem.getPrice().multiply(BigDecimal.valueOf(cartItem.getQuantity()));
                        String formattedVariant = Optional.ofNullable(skusInfoMap.get(cartItem.getSkuId()))
                                .map(info -> info.getSelections().stream()
                                        .map(o -> o.getGroupName() + ": " + o.getLabel())
                                        .collect(Collectors.joining(" - ")))
                                .orElse("Phiên bản tiêu chuẩn");
                        return OrderItemEntity.builder()
                                .order(order) // Gán quan hệ 2 chiều
                                .skuCode(skusInfoMap.get(cartItem.getSkuId()).getSkuCode())
                                .productThumbnail(skusInfoMap.get(cartItem.getSkuId()).getThumbnail())
                                .variantName(formattedVariant)
                                .skuId(cartItem.getSkuId())
                                .quantity(cartItem.getQuantity())
                                .unitPrice(cartItem.getPrice())
                                .productId(skusInfoMap.get(cartItem.getSkuId()).getSpuId())
                                .productName(skusInfoMap.get(cartItem.getSkuId()).getSpuName())
                                .subTotal(itemSubTotal)
                                .build();
                    }
            ).collect(Collectors.toCollection(ArrayList::new));
            order.setOrderItems(orderItems);
            OrderEntity newOrder = orderRepository.save(order);
            List<OrderItemCheckForm> checkFormList = cart.getItems().stream()
                    .map(it -> OrderItemCheckForm
                            .builder()
                            .orderId(newOrder.getId().toString())
                            .skuCode(skusInfoMap.get(it.getSkuId()).getSkuCode())
                            .quantity(it.getQuantity())
                            .build())
                    .toList();
            MDC.put("sagaStep", "INVENTORY_RESERVE");
            log.info("Đang gọi Inventory Service để giữ hàng và nhặt số Serial");
            try {
                List<InventoryAvailableDto> inventoryResponses = inventoryFeignClient
                        .checkAvailability(OrderCreateInventoryForm.builder()
                                .items(checkFormList)
                                .orderId(order.getId().toString())
                                .orderNumber(orderNumber)
                                .build());
                for (InventoryAvailableDto invDto : inventoryResponses) {
                    OrderItemEntity item = order.getOrderItems().stream()
                            .filter(i -> i.getSkuCode().equals(invDto.getSkuCode()))
                            .findFirst()
                            .orElseThrow();

                    // Lưu danh sách Serial vào Item
                    for (String sn : invDto.getSerialNumbers()) {
                        OrderItemSerialEntity serialEntity = new OrderItemSerialEntity();
                        serialEntity.setSerialNumber(sn);
                        serialEntity.setOrderItem(item);
                        item.getSerials().add(serialEntity);
                    }
                }

                // 4. Thành công: Chuyển trạng thái
                newOrder.setStatus(form.getPaymentMethod().equals(PaymentMethod.COD) ? CONFIRMED : RESERVED);
                log.info("Giữ hàng thành công, nhận được {} mã Serial", inventoryResponses.size());
            } catch (FeignException.BadRequest e) {
                // Hết hàng
                MDC.put("status", "FAILED");
                log.error("Hết hàng khi đang giữ chỗ: {}", e.getMessage());
                orderRepository.updateStatus(order.getId(), OUT_OF_STOCK);
                throw new BusinessException(INVENTORY_QUANTITY_FAIL);
            } catch (Exception e) {
                // Lỗi hệ thống khác
                MDC.put("status", "FAILED");
                log.error("Lỗi hệ thống khi gọi Inventory: {}", e.getMessage());
                orderRepository.updateStatus(order.getId(), CANCELLED);
                throw new BusinessException(INVENTORY_QUANTITY_FAIL);
            }
            OrderEntity initialOrder = orderRepository.save(order);
            String paymentUrl = null;
            switch (form.getPaymentMethod()) {
                case COD:
                    cart.getItems().clear();
                    cartRepository.save(cart);
                    orderStatisticsService.updateStatsOnNewOrder(initialOrder);
                    break;
                case VNPAY:
                    MDC.put("sagaStep", "PAYMENT_INITIATION");
                    MDC.put("paymentMethod", form.getPaymentMethod().name());
                    log.info("Đang khởi tạo cổng thanh toán: {}", form.getPaymentMethod());
                    PaymentResponse paymentVnpay = paymentFeignClient
                            .getVnpayUrl(initialOrder.getFinalAmount().longValue(), null, "ORDER", initialOrder.getOrderNumber(), req.getRemoteAddr());
                    paymentUrl = paymentVnpay.getUrl();
                    initialOrder.setPaymentUrl(paymentUrl);
                    initialOrder.setExpiresAt(LocalDateTime.now().plusMinutes(17));
                    initialOrder.setStatus(AWAITING_PAYMENT);
                    break;
                case BANK:
                    MDC.put("sagaStep", "PAYMENT_INITIATION");
                    MDC.put("paymentMethod", form.getPaymentMethod().name());
                    log.info("Đang khởi tạo cổng thanh toán: {}", form.getPaymentMethod());
                    SePayResponsive paymentSepay = paymentFeignClient
                            .getSepayUrl(initialOrder.getFinalAmount().longValue(), "ORDER", initialOrder.getOrderNumber());
                    paymentUrl = paymentSepay.getData();
                    initialOrder.setPaymentUrl(paymentUrl);
                    initialOrder.setExpiresAt(LocalDateTime.now().plusMinutes(17));
                    initialOrder.setStatus(AWAITING_PAYMENT);
                    break;
                case WALLET:
                    MDC.put("sagaStep", "PAYMENT_WALLET_EXECUTION");
                    MDC.put("paymentMethod", "WALLET");
                    log.info("Bắt đầu thực hiện thanh toán bằng ví nội bộ cho đơn hàng: {}", initialOrder.getOrderNumber());
                    InternalPaymentForm internalPaymentForm = InternalPaymentForm.builder()
                            .amount(initialOrder.getFinalAmount().longValue())
                            .orderNumber(initialOrder.getOrderNumber())
                            .build();
                    try {
                        log.info("Đang gửi yêu cầu trừ tiền sang Payment Service...");
                        WalletResponse walletResponse = paymentFeignClient.payInternalOrder(internalPaymentForm);
                        MDC.put("status", "SUCCESS");
                        log.info("Thanh toán ví thành công. Message từ ví: {}", walletResponse.getMessage());
                        initialOrder.setPaymentStatus(PaymentStatus.SUCCEEDED);
                        initialOrder.setStatus(CONFIRMED);
                        initialOrder.setNote(walletResponse.getMessage());
                        log.info("Đang dọn dẹp giỏ hàng và cập nhật thống kê...");
                        cart.getItems().clear();
                        cartRepository.save(cart);
                        orderStatisticsService.updateStatsOnNewOrder(initialOrder);
                    } catch (Exception e) {
                        MDC.put("status", "FAILED");
                        MDC.put("errorType", e.getClass().getSimpleName());
                        log.error("Thanh toán ví thất bại! Lý do: {}", e.getMessage());
                        throw new BusinessException(PAYMENT_ERROR);
                    }
                default:
                    break;
            }
            OrderEntity savedOrder = orderRepository.save(initialOrder);
            MDC.put("status", "SUCCESS");
            log.info("Đơn hàng được tạo thành công với trạng thái: {}", newOrder.getStatus());
            return OrderResponse.builder()
                    .orderNumber(orderNumber)
                    .orderStatus(savedOrder.getStatus())
                    .paymentMethod(savedOrder.getPaymentMethod())
                    .paymentUrl(paymentUrl)
                    .build();
        } catch (Exception e) {
            MDC.put("status", "ERROR");
            log.error("Lỗi khi tạo đơn hàng: {}", e.getMessage());
            throw e;
        } finally {
            MDC.clear();
        }
    }


    private boolean isValidTransitionForAdmin(OrderStatus from, OrderStatus to) {
        if (from == null || to == null) return false;
        if (from == to) return true;

        // 1. Trạng thái cuối (Terminal States) - Không thể chuyển đi đâu nữa
        if (from == OrderStatus.COMPLETED || from == OrderStatus.CANCELLED ||
                from == OrderStatus.EXPIRED || from == OrderStatus.OUT_OF_STOCK) {
            return false;
        }

        // 2. Định nghĩa các luồng đi hợp lệ (White-listing)
        return switch (from) {
            case CREATED -> to == OrderStatus.RESERVED ||       // Hệ thống giữ được hàng
                    to == OrderStatus.OUT_OF_STOCK ||  // API Kho báo hết
                    to == OrderStatus.CANCELLED ||
                    to == OrderStatus.EXPIRED;

            case RESERVED -> to == OrderStatus.AWAITING_PAYMENT ||
                    to == OrderStatus.CANCELLED ||
                    to == OrderStatus.EXPIRED;

            case AWAITING_PAYMENT -> to == OrderStatus.PAID ||
                    to == OrderStatus.CANCELLED ||
                    to == OrderStatus.EXPIRED;

            case PAID -> to == OrderStatus.CONFIRMED ||
                    to == OrderStatus.SHIPPING ||
                    to == OrderStatus.CANCELLED; // Cần logic Refund đi kèm

            case CONFIRMED -> to == OrderStatus.SHIPPING ||
                    to == OrderStatus.CANCELLED;

            case SHIPPING -> to == OrderStatus.DELIVERED ||
                    to == OrderStatus.RETURNED; // Trường hợp shipper báo khách không nhận/quay đầu

            case DELIVERED -> to == OrderStatus.COMPLETED ||
                    to == OrderStatus.RETURNED;

            case RETURNED -> to == OrderStatus.COMPLETED ||
                    to == OrderStatus.CANCELLED;

            default -> false;
        };
    }


}
