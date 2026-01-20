package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.constants.Constants;
import com.ecommerce.inventoryservice.dto.event.OrderEventPayload;
import com.ecommerce.inventoryservice.dto.event.StockUpdatePayload;
import com.ecommerce.inventoryservice.dto.exception.BusinessException;
import com.ecommerce.inventoryservice.dto.request.InventoryAdjustRequest;
import com.ecommerce.inventoryservice.dto.request.OrderItemCheckForm;
import com.ecommerce.inventoryservice.dto.request.StockResultPayload;
import com.ecommerce.inventoryservice.dto.response.InventoryAdjustResponse;
import com.ecommerce.inventoryservice.dto.response.InventoryDTO;
import com.ecommerce.inventoryservice.dto.response.InventoryStatus;
import com.ecommerce.inventoryservice.dto.response.cart.InventoryCartDto;
import com.ecommerce.inventoryservice.entity.*;
import com.ecommerce.inventoryservice.repository.InventoryHistoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.OutboxRepository;
import com.ecommerce.inventoryservice.repository.ProcessedEventRepository;
import com.ecommerce.inventoryservice.service.InventoryService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
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

    // 2. Giữ hàng (Khi khách bấm đặt hàng)
    @Transactional
    @Override
    public void reserveStock(OrderEventPayload payload) throws JsonProcessingException {
        String idempotencyKey = "ORDER_RESERVE:" + payload.getOrderId();
        if (processedEventRepository.existsById(idempotencyKey)) return;
        boolean allItemsReserved = true;
        List<InventoryHistoryEntity> allHistory = new ArrayList<>();
        for (var item : payload.getItems()) {
            // 2. Thực hiện Atomic Update để giữ chỗ
            int updatedRows = inventoryRepo.reserveStock(item.getSkuCode(), item.getQuantity());


// Lưu vào cột payload của bảng t_outbox_events
            if (updatedRows == 0) {
                log.error("Sản phẩm {} hết hàng", item.getSkuCode());
                allItemsReserved = false;
                break;
            }

            // 3. Lấy thông tin kho sau khi update để ghi lịch sử (stockAfter)
            // Lưu ý: stockAfter ở đây thường là số lượng "Có thể bán" (Available = Total - Reserved)
            InventoryEntity updatedInv = inventoryRepo.findBySkuCode(item.getSkuCode())
                    .orElseThrow();
            int availableAfter = updatedInv.getTotalStock() - updatedInv.getReservedStock();

            // 4. Lưu lịch sử biến động
            InventoryHistoryEntity history = new InventoryHistoryEntity();
            history.setSkuCode(item.getSkuCode());
            history.setQuantityChange(-item.getQuantity()); // Giảm lượng có thể bán nên để âm
            history.setStockAfter(availableAfter);
            history.setType(InventoryType.RESERVED); // Enum bạn đã định nghĩa
            history.setReferenceId(payload.getOrderId());
            allHistory.add(history);
        }
        historyRepo.saveAll(allHistory);
        StockResultPayload response = StockResultPayload.builder()
                .orderId(payload.getOrderId())
                .userId(payload.getUserId())
                .status(allItemsReserved ? "SUCCESS" : "FAILED")
                .build();
        OutboxEvent outbox = OutboxEvent.builder()
                .id(UUID.randomUUID().toString())
                .aggregateType("stock") // Sẽ sinh ra topic inventory-service.stock.events
                .aggregateId(payload.getOrderId())
                .type(allItemsReserved ? "STOCK_RESERVED" : "STOCK_FAILED")
                .payload(objectMapper.writeValueAsString(response))
                .createdAt(LocalDateTime.now())
                .build();

        outboxRepo.save(outbox);
        // 5. Đánh dấu đã xử lý xong event
        processedEventRepository.save(new ProcessedEvent(idempotencyKey, Instant.now()));
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
        Integer qtyObj = req.getQuantity();
        InventoryType type = req.getType();
        String ref = (req.getNote() == null || req.getNote().isBlank())
                ? "MANUAL_ADJUST"
                : req.getNote().trim();
        if (sku.isBlank() || type == null || qtyObj == null || qtyObj <= 0)
            throw new BusinessException(VALIDATE_FAIL);


        int qty = qtyObj;

        // snapshot before
        InventoryEntity before = inventoryRepo.findBySkuCode(sku)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        int beforeTotal = safe(before.getTotalStock());
        int beforeReserved = safe(before.getReservedStock());

        int updated;
        int change; // quantityChange log history

        switch (type) {
            case IMPORT -> {
                updated = inventoryRepo.incTotal(sku, qty);
                change = qty;
            }
            case EXPORT -> {
                updated = inventoryRepo.decTotalIfAvailable(sku, qty);
                if (updated == 0) {
                    throw new BusinessException(VALIDATE_FAIL);
                }
                change = -qty;
            }
            case ADJUST -> {
                updated = inventoryRepo.setTotalIfNotBelowReserved(sku, qty);
                if (updated == 0) {
                    throw new BusinessException(VALIDATE_FAIL);
                }
                change = qty - beforeTotal; // delta
            }
            default -> throw new BusinessException(VALIDATE_FAIL);
        }

        if (updated == 0) {
            // trường hợp rất hiếm: sku không tồn tại trong câu UPDATE
            throw new BusinessException(VALIDATE_FAIL);
        }

        // snapshot after (1 lần thôi)
        InventoryEntity after = inventoryRepo.findBySkuCode(sku)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));

        int afterTotal = safe(after.getTotalStock());
        int afterReserved = safe(after.getReservedStock());

        // log history: quantityChange = change, stockAfter = afterTotal
        logHistory(sku, change, afterTotal, type, ref);

        return InventoryAdjustResponse.builder()
                .skuCode(sku)
                .totalStock(afterTotal)
                .reservedStock(afterReserved)
                .availableStock(afterTotal - afterReserved)
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

    @Override
    public boolean checkAvailability(List<OrderItemCheckForm> items) {
        List<String> skuCodes = items.stream()
                .map(OrderItemCheckForm::getSkuCode)
                .toList();

        // 2. Truy vấn tất cả các SKU liên quan trong 1 lần (tối ưu hiệu năng)
        List<InventoryEntity> inventories = inventoryRepo.findBySkuCodeIn(skuCodes);

        // Tạo Map để tra cứu nhanh cho bước so sánh
        Map<String, InventoryEntity> inventoryMap = inventories.stream()
                .collect(Collectors.toMap(InventoryEntity::getSkuCode, i -> i));

        // 3. Kiểm tra từng item trong yêu cầu
        for (OrderItemCheckForm item : items) {
            InventoryEntity inv = inventoryMap.get(item.getSkuCode());

            // Nếu không tìm thấy SKU hoặc số lượng khả dụng không đủ
            if (inv == null) return false;

            int availableStock = inv.getTotalStock() - inv.getReservedStock();
            if (availableStock < item.getQuantity()) {
                return false; // Chỉ cần 1 món không đủ là trả về false ngay
            }
        }

        return true; // Tất cả các món đều đủ hàng
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

    private int safe(Integer v) {
        return v == null ? 0 : v;
    }
}
