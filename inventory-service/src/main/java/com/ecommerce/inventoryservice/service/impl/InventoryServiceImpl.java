package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.event.*;
import com.ecommerce.inventoryservice.dto.exception.BusinessException;
import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.request.order.OrderCreateInventoryForm;
import com.ecommerce.inventoryservice.dto.request.order.OrderItemCheckForm;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.dto.response.InventoryStatus;
import com.ecommerce.inventoryservice.dto.response.cart.InventoryCartDto;
import com.ecommerce.inventoryservice.dto.response.order.InventoryAvailableDto;
import com.ecommerce.inventoryservice.entity.*;
import com.ecommerce.inventoryservice.repository.*;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.ecommerce.inventoryservice.constants.Constants.*;

@Slf4j
@Service
public class InventoryServiceImpl implements InventoryService {
    @Autowired
    InventoryHistoryRepository historyRepo;
    @Autowired
    InventoryRepository inventoryRepo;
    @Autowired
    private ProcessedEventRepository processedEventRepository;
    @Autowired
    private OutboxRepository outboxRepo;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InventoryReservationRepository reservationRepo;
    @Autowired
    private InventoryItemRepository inventoryItemRepo;

    @Override
    public Page<InventoryDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        Pageable pageable = PageRequest.of(
                Math.max(page, 0),
                Math.max(size, 1),
                parseSort(sort)
        );

        String kw = keyword == null ? "" : keyword.trim();
        Page<InventoryEntity> pageEntity =
                kw.isBlank()
                        ? inventoryRepo.findAll(pageable)
                        : inventoryRepo.findBySkuCodeContainingIgnoreCase(kw, pageable);

        return pageEntity.map(p -> {
            int total = p.getTotalStock();
            int reserved = p.getReservedStock();
            int available = total - reserved;

            int min = p.getMinStockLevel();
            boolean lowStock = available > 0 && available <= min;
            InventoryStatus status;
            if (available <= 0) {
                status = InventoryStatus.OUT_OF_STOCK;
            } else if (lowStock) {
                status = InventoryStatus.LOW_STOCK;
            } else {
                status = InventoryStatus.IN_STOCK;
            }
            return InventoryDTO.builder()
                    .id(p.getId().toString())
                    .skuCode(p.getSkuCode())
                    .totalStock(total)
                    .reservedStock(reserved)
                    .availableStock(available)
                    .lowStock(lowStock)
                    .minStockLevel(min)
                    .status(status)
                    .build();
        });
    }

    @Override
    public void createInventory(String skuCode, Integer initialStock) {
        if (inventoryRepo.existsBySkuCode(skuCode)) {
            return; // Đã tồn tại thì bỏ qua hoặc cộng dồn (tùy logic)
        }

        InventoryEntity inv = new InventoryEntity();
        inv.setSkuCode(skuCode);
        inv.setTotalStock(initialStock);
        inv.setReservedStock(0);
        inv.setMinStockLevel(5);

        inventoryRepo.save(inv);

        // Log history
        logHistory(skuCode, initialStock, initialStock, InventoryType.IMPORT, "INITIAL_SETUP");
    }

    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "skuCode",
            "totalStock",
            "reservedStock",
            "minStockLevel"
    );

    private Sort parseSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return Sort.by(Sort.Direction.ASC, "skuCode");
        }

        String[] parts = sort.split(":");
        String field = parts[0].trim();
        String dir = parts.length > 1 ? parts[1].trim() : "asc";

        if (!ALLOWED_SORT_FIELDS.contains(field)) {
            field = "skuCode"; // fallback an toàn
        }

        Sort.Direction direction =
                "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        return Sort.by(direction, field);
    }

    //new
    // 2. Giữ hàng (Khi khách bấm đặt hàng)
    @Transactional
    @Override
    public void reserveStock(OrderCreatedPayload payload) throws JsonProcessingException {
        UUID orderId = UUID.fromString(payload.getOrderId());
        if (reservationRepo.existsByOrderId(orderId)) {
            return;
        }
        List<String> skuCodes = payload.getItems().stream().map(OrderCreatedPayload.OrderItemPayload::getSkuCode).toList();
        Map<String, InventoryEntity> stockMap = inventoryRepo.findBySkuCodeInForUpdate(skuCodes)
                .stream().collect(Collectors.toMap(InventoryEntity::getSkuCode, s -> s));
        List<InventoryReserveFailedPayload.FailedItem> failed = new ArrayList<>();
        for (var it : payload.getItems()) {
            // 2. Thực hiện Atomic Update để giữ chỗ
            InventoryEntity s = stockMap.get(it.getSkuCode());
            long available = (s == null) ? 0L : s.getTotalStock() - s.getReservedStock();
            if (available < it.getQuantity()) {
                failed.add(InventoryReserveFailedPayload.FailedItem.builder()
                        .skuCode(it.getSkuCode())
                        .requested(it.getQuantity())
                        .available(available)
                        .build());
            }
        }
        if (!failed.isEmpty()) {
            emitReserveFailed(payload.getOrderId(), failed);
            return;
        }
        List<InventoryHistoryEntity> allHistory = new ArrayList<>();
        for (var it : payload.getItems()) {
            InventoryEntity s = stockMap.get(it.getSkuCode());
            s.setReservedStock(s.getReservedStock() + it.getQuantity());
            InventoryHistoryEntity history = new InventoryHistoryEntity();
            history.setSkuCode(it.getSkuCode());
            history.setQuantityChange(-it.getQuantity());
            history.setType(InventoryType.RESERVED);
            history.setReferenceId(orderId.toString());
            allHistory.add(history);
        }
        historyRepo.saveAll(allHistory);
        inventoryRepo.saveAll(stockMap.values());
        UUID reservationId = UUID.randomUUID();
        LocalDateTime expireAt = LocalDateTime.now().plusMinutes(15);
        ReservationEntity reservation = ReservationEntity.builder()
                .id(reservationId)
                .orderId(orderId)
                .status(ReservationStatus.ACTIVE)
                .expireAt(expireAt)
                .createdAt(LocalDateTime.now())
                .build();
        for (var it : payload.getItems()) {
            reservation.addItem(ReservationItemEntity.builder()
                    .skuCode(it.getSkuCode())
                    .qty(it.getQuantity())
                    .build());
        }
        try {
            reservationRepo.save(reservation);
        } catch (DataIntegrityViolationException dup) {
            throw dup;
        }

        // 4) Emit Inventory.Reserved (OUTBOX)
        emitReserved(payload.getOrderId(), reservationId.toString(), expireAt, payload.getItems());

        // 5. Đánh dấu đã xử lý xong event
    }

    //new
    private void emitReserved(String orderId, String reservationId, LocalDateTime expireAt,
                              List<OrderCreatedPayload.OrderItemPayload> items) {
        try {
            InventoryReservedPayload payload = InventoryReservedPayload.builder()
                    .orderId(orderId)
                    .reservationId(reservationId)
                    .expireAt(expireAt)
                    .items(items.stream()
                            .map(i -> InventoryReservedPayload.Item.builder()
                                    .skuCode(i.getSkuCode())
                                    .qty(i.getQuantity())
                                    .build())
                            .toList())
                    .build();

            outboxRepo.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(orderId)                  // correlate theo orderId
                    .type("Inventory.Reserved")            // eventType
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    //new
    private void emitReserveFailed(String orderId, List<InventoryReserveFailedPayload.FailedItem> failed) {
        try {
            InventoryReserveFailedPayload payload = InventoryReserveFailedPayload.builder()
                    .orderId(orderId)
                    .reason("OUT_OF_STOCK")
                    .failedItems(failed)
                    .build();

            outboxRepo.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(orderId)
                    .type("Inventory.ReserveFailed")
                    .payload(objectMapper.writeValueAsString(payload))
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 3. Thanh toán thành công -> Trừ kho thật
    @Transactional
    public void confirmOrder(String skuCode, Integer quantity, String orderId) {
        inventoryRepo.confirmStock(skuCode, quantity);

        // Lấy lại tồn kho hiện tại để lưu log cho đúng
        // (Lưu ý: query này chỉ để lấy số liệu ghi log, không ảnh hưởng logic trừ kho)
        InventoryEntity current = inventoryRepo.findBySkuCode(skuCode).orElseThrow(
                () -> new BusinessException(VALIDATE_FAIL)
        );
        logHistory(skuCode, -quantity, current.getTotalStock(), InventoryType.SOLD, orderId);
    }

    // ... Các hàm cancel, import stock khác
    @Override
    public void logHistory(String sku, int change, int stockAfter, InventoryType type, String ref) {
        InventoryHistoryEntity history = new InventoryHistoryEntity();
        history.setSkuCode(sku);
        history.setQuantityChange(change);
        history.setStockAfter(stockAfter);
        history.setType(type);
        history.setReferenceId(ref);
        historyRepo.save(history);
    }

    @Transactional
    @Override
    public InventoryAdjustResponse adjustInventory(InventoryAdjustRequest req) {
        String sku = req.getSkuCode() == null ? "" : req.getSkuCode().trim();
        InventoryType type = req.getType();
        String ref = (req.getNote() == null || req.getNote().isBlank()) ? "MANUAL_ADJUST" : req.getNote().trim();

        // 1. Validation cơ bản
        if (sku.isBlank() || type == null) throw new BusinessException(VALIDATE_FAIL);

        // 2. Lấy thông tin kho tổng hiện tại
        InventoryEntity inventory = inventoryRepo.findBySkuCode(sku)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        int change = 0;

        switch (type) {
            case IMPORT -> {
                List<String> sns = req.getSerialNumbers();
                if (sns == null || sns.isEmpty()) throw new BusinessException(VALIDATE_FAIL);

                // A. Kiểm tra trùng mã Serial trong hệ thống
                if (inventoryItemRepo.existsBySerialNumberIn(sns)) {
                    throw new BusinessException(SERIAL_EXISTED);
                }

                // B. Thêm mới các Serial vào bảng chi tiết (Trạng thái AVAILABLE)
                List<InventoryItemEntity> newItems = sns.stream().map(sn -> {
                            InventoryItemEntity item = new InventoryItemEntity();
                            item.setInventory(inventory);
                            item.setSerialNumber(sn);
                            item.setSkuCode(sku);
                            item.setStatus(InventoryItemStatus.AVAILABLE);
                            return item;
                        }
                ).toList();
                inventoryItemRepo.saveAll(newItems);

                // C. Cộng vào kho tổng
                change = sns.size();
                inventoryRepo.incTotal(sku, change);
            }

            case EXPORT -> {
                List<String> sns = req.getSerialNumbers();
                if (sns == null || sns.isEmpty()) throw new BusinessException(VALIDATE_FAIL);

                // A. Kiểm tra xem các Serial này có trong kho và đang AVAILABLE không
                List<InventoryItemEntity> items = inventoryItemRepo.findBySerialNumberInAndStatus(sns, InventoryItemStatus.AVAILABLE);
                if (items.size() < sns.size()) throw new BusinessException(SERIAL_EXISTED);

                // B. Đổi trạng thái Serial sang SOLD hoặc EXPORTED
                items.forEach(i -> i.setStatus(InventoryItemStatus.SOLD));
                inventoryItemRepo.saveAll(items);

                // C. Trừ khỏi kho tổng
                change = -sns.size();
                inventoryRepo.decTotalIfAvailable(sku, sns.size());
            }

            case ADJUST -> {
                // Loại này dùng để "Chốt số" sau khi kiểm kê, thường không đi kèm Serial cụ thể
                if (req.getQuantity() == null) throw new BusinessException(VALIDATE_FAIL);
                int beforeTotal = inventory.getTotalStock();
                inventoryRepo.setTotalIfNotBelowReserved(sku, req.getQuantity());
                change = req.getQuantity() - beforeTotal;
            }
        }

        // 3. Log lịch sử kho
        InventoryEntity after = inventoryRepo.findBySkuCode(sku).get();
        logHistory(sku, change, after.getTotalStock(), type, ref);

        return InventoryAdjustResponse.builder()
                .skuCode(sku)
                .totalStock(after.getTotalStock())
                .reservedStock(after.getReservedStock())
                .availableStock(after.getTotalStock() - after.getReservedStock())
                .build();
    }

    @Override
    public List<InventoryCartDto> getStockOfSkus(List<String> skuCodes) {
        List<InventoryEntity> inventories = inventoryRepo.findAllBySkuCodeIn(skuCodes);
        return inventories.stream().map(
                i -> InventoryCartDto.builder()
                        .skuCode(i.getSkuCode())
                        .quantity(i.getTotalStock() - i.getReservedStock())
                        .build()
        ).toList();
    }

    @Transactional
    @Override
    public List<InventoryAvailableDto> checkAvailability(OrderCreateInventoryForm form) {
        try {
            MDC.put("orderNo", form.getOrderNumber());
            MDC.put("sagaStep", "INVENTORY_RESERVE");
            MDC.put("status", "STARTED");

            log.info("Bắt đầu giữ hàng cho đơn hàng");
            List<String> skuCodes = form.getItems().stream().map(OrderItemCheckForm::getSkuCode).toList();

            // 1. Lấy và KHÓA các dòng inventory tổng (Pessimistic Lock)
            List<InventoryEntity> inventories = inventoryRepo.findBySkuCodeIn(skuCodes);
            Map<String, InventoryEntity> inventoryMap = inventories.stream()
                    .collect(Collectors.toMap(InventoryEntity::getSkuCode, i -> i));

            List<InventoryAvailableDto> result = new ArrayList<>();

            // 2. Kiểm tra và thực hiện giữ hàng cho từng item
            for (OrderItemCheckForm item : form.getItems()) {
                InventoryEntity inv = inventoryMap.get(item.getSkuCode());

                // Kiểm tra tồn kho tổng quát
                if (inv == null || (inv.getTotalStock() - inv.getReservedStock()) < item.getQuantity()) {
                    throw new BusinessException(OUT_OF_STOCK);
                }

                // A. Cập nhật số lượng reserved trong bảng tổng
                inv.setReservedStock(inv.getReservedStock() + item.getQuantity());
                inventoryRepo.save(inv);

                // B. Nhặt các Serial cụ thể trong bảng chi tiết
                List<InventoryItemEntity> availableSerials = inventoryItemRepo.findTopBySkuCodeAndStatus(
                        item.getSkuCode(),
                        InventoryItemStatus.AVAILABLE,
                        PageRequest.of(0, item.getQuantity())
                );

                if (availableSerials.size() < item.getQuantity()) {
                    throw new BusinessException(OUT_OF_STOCK);
                }

                List<String> serialNumbers = new ArrayList<>();
                for (InventoryItemEntity serialItem : availableSerials) {
                    serialItem.setStatus(InventoryItemStatus.RESERVED);
                    serialItem.setOrderId(UUID.fromString(form.getOrderId())); // Đánh dấu máy này thuộc đơn hàng nào
                    serialItem.setOrderNumber(form.getOrderNumber());
                    inventoryItemRepo.save(serialItem);
                    serialNumbers.add(serialItem.getSerialNumber());
                }

                // C. Thêm vào kết quả trả về cho Order Service
                result.add(InventoryAvailableDto.builder()
                        .serialNumbers(serialNumbers)
                        .skuCode(item.getSkuCode())
                        .available(true)
                        .build());
            }
            MDC.put("status", "SUCCESS");
            log.info("Giữ hàng thành công");
            return result;
        } catch (Exception e) {
            MDC.put("status", "FAILED");
            log.error("Lỗi giữ hàng: {}", e.getMessage());
            throw e;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    @Override
    public void restoreStock(List<StockUpdatePayload.StockItem> items) {
        for (StockUpdatePayload.StockItem item : items) {
            int updatedRows = inventoryRepo.restoreStock(item.getSkuCode(), item.getQuantity());

            if (updatedRows == 0) {
                log.error("Failed to restore stock for SKU: {}. Maybe insufficient reserved quantity.", item.getSkuCode());
                throw new RuntimeException("Restore stock failed for SKU: " + item.getSkuCode());
            }
            log.info("Restored SKU: {} | Quantity: {}", item.getSkuCode(), item.getQuantity());
        }
    }

    @Override
    public void finalizeDeduction(String orderId, List<StockUpdatePayload.StockItem> items) {
        for (StockUpdatePayload.StockItem item : items) {
            int updatedRows = inventoryRepo.confirmDeduction(item.getSkuCode(), item.getQuantity());

            if (updatedRows == 0) {
                log.warn("SKU {} reserved quantity was already cleared or mismatched for Order {}",
                        item.getSkuCode(), orderId);
                // Có thể không cần throw exception ở đây tùy vào độ chặt chẽ bạn muốn
            }
            InventoryEntity current = inventoryRepo.findBySkuCode(item.getSkuCode()).orElseThrow(
                    () -> new BusinessException(VALIDATE_FAIL)
            );
            logHistory(item.getSkuCode(), -item.getQuantity(), current.getTotalStock(), InventoryType.SOLD, orderId);
            log.info("Finalized deduction for Order: {} | SKU: {}", orderId, item.getSkuCode());
        }
    }

    @Override
    @Transactional // Rất quan trọng để đảm bảo tính nguyên tử
    public List<InventoryAvailableDto> reReserve(ReReserveRequest request) {
        // 1. Lấy danh sách SKU cần tìm
        List<String> skuCodes = request.getItems().stream()
                .map(ReReserveRequest.ReReserveItem::getSkuCode).toList();

        // 2. SQL: Lấy tất cả Serial tiềm năng (Ưu tiên máy cũ của đơn hàng này)
        List<InventoryItemEntity> allAvailable = inventoryItemRepo.findAllPotentialSerials(skuCodes, request.getOrderNumber());

        // 3. Nhóm theo SKU để xử lý trên RAM
        Map<String, List<InventoryItemEntity>> serialsBySku = allAvailable.stream()
                .collect(Collectors.groupingBy(InventoryItemEntity::getSkuCode));

        List<InventoryAvailableDto> result = new ArrayList<>();

        // List để lưu các Serial sẽ được cập nhật SOLD
        List<InventoryItemEntity> itemsToSold = new ArrayList<>();
        // Map để lưu số lượng cần trừ ở kho tổng
        Map<String, Integer> skuToSubtract = new HashMap<>();

        for (var itemReq : request.getItems()) {
            List<InventoryItemEntity> pool = serialsBySku.getOrDefault(itemReq.getSkuCode(), new ArrayList<>());

            if (pool.size() < itemReq.getQuantity()) {
                // Nếu không đủ hàng thì throw lỗi để Order Service thực hiện Refund tiền cho khách
                throw new BusinessException(OUT_OF_STOCK);
            }

            // 4. Nhặt đúng số lượng cần thiết
            List<InventoryItemEntity> selected = pool.subList(0, itemReq.getQuantity());

            selected.forEach(s -> {
                s.setStatus(InventoryItemStatus.SOLD); // ĐẶT LÀ SOLD LUÔN
                s.setOrderId(UUID.fromString(request.getOrderId()));
                s.setOrderNumber(request.getOrderNumber());
                s.setSoldAt(LocalDateTime.now()); // Lưu dấu vết ngày bán
                itemsToSold.add(s);
            });

            skuToSubtract.put(itemReq.getSkuCode(), itemReq.getQuantity());

            result.add(InventoryAvailableDto.builder()
                    .serialNumbers(selected.stream().map(InventoryItemEntity::getSerialNumber).toList())
                    .skuCode(itemReq.getSkuCode())
                    .available(true)
                    .build());
        }

        // 5. CẬP NHẬT DATABASE
        // A. Lưu trạng thái các Serial
        inventoryItemRepo.saveAll(itemsToSold);

        // B. Trừ kho tổng (Chỉ trừ total_stock, vì hàng này nhặt từ nguồn AVAILABLE)
        skuToSubtract.forEach((sku, qty) -> {
            inventoryRepo.confirmStock(sku, qty);
        });

        return result;
    }

    @Transactional
    public void releaseForOrder(InventoryReserveRequestedPayload p) throws JsonProcessingException {

        for (var item : p.getItems()) {
            int updated = inventoryRepo.release(item.getSkuCode(), item.getQty());
            if (updated == 0) {
                log.warn("Release skipped sku={} qty={} for orderId={} (maybe already released or not reserved)",
                        item.getSkuCode(), item.getQty(), p.getOrderId());
            }
        }

        // (Optional) bắn event báo order biết đã nhả xong
        StockReleasedPayload ok = StockReleasedPayload.builder()
                .orderId(p.getOrderId())
                .orderNumber(p.getOrderNumber())
                .releasedAt(LocalDateTime.now())
                .build();

        outboxRepo.save(OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType("order")          // => inventory-service.order.events
                .aggregateId(p.getOrderId())
                .type("STOCK_RELEASED")
                .payload(objectMapper.writeValueAsString(ok))
                .createdAt(LocalDateTime.now())
                .build());
    }

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
