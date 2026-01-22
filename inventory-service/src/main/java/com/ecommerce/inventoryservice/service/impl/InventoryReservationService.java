package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.dto.event.InventoryCommitRequestedPayload;
import com.ecommerce.inventoryservice.dto.event.InventoryReleaseRequestedPayload;
import com.ecommerce.inventoryservice.dto.exception.BusinessException;
import com.ecommerce.inventoryservice.entity.*;
import com.ecommerce.inventoryservice.repository.InventoryHistoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryRepository;
import com.ecommerce.inventoryservice.repository.InventoryReservationRepository;
import com.ecommerce.inventoryservice.repository.OutboxRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.ecommerce.inventoryservice.constants.Constants.*;

@Service
public class InventoryReservationService {
    @Autowired
    private InventoryReservationRepository inventoryReservationRepository;
    @Autowired
    private OutboxRepository outboxRepository;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private InventoryHistoryRepository inventoryHistoryRepository;
    @Autowired
    private InventoryRepository inventoryRepository;
    @Transactional
    public void commitReservation(InventoryCommitRequestedPayload ev) {
        UUID reservationId = UUID.fromString(ev.getReservationId());

        ReservationEntity reservation = inventoryReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(RESERVATION_NOT_FOUND));

        // ✅ Idempotent
        if (reservation.getStatus() == ReservationStatus.COMMITTED) {
            return;
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException(RESERVATION_STATUS_FAIL);
        }

        List<String> skuCodes = reservation.getItems().stream()
                .map(ReservationItemEntity::getSkuCode)
                .sorted() // tránh deadlock
                .toList();

        Map<String, InventoryEntity> stockMap = inventoryRepository.findBySkuCodeInForUpdate(skuCodes)
                .stream()
                .collect(Collectors.toMap(InventoryEntity::getSkuCode, s -> s));

        for (ReservationItemEntity item : reservation.getItems()) {
            InventoryEntity stock = stockMap.get(item.getSkuCode());
            long qty = item.getQty();

            if (stock == null) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            if (stock.getReservedStock() < qty) {
                throw new BusinessException(STOCK_RESERVED_INCONSISTENT);
            }
            if (stock.getTotalStock() < qty) {
                throw new BusinessException(STOCK_TOTAL_INCONSISTENT);
            }

            stock.setReservedStock((int) (stock.getReservedStock() -  qty));
            stock.setTotalStock((int) (stock.getTotalStock() - qty)); // ✅ bán xong → giảm tổng
        }

        inventoryRepository.saveAll(stockMap.values());

        reservation.setStatus(ReservationStatus.COMMITTED);
        reservation.setUpdatedAt(LocalDateTime.now());
        inventoryReservationRepository.save(reservation);

        emitInventoryCommitted(ev.getOrderId(), ev.getReservationId());
    }
    @Transactional
    public void releaseReservation(InventoryReleaseRequestedPayload ev) {
        UUID reservationId = UUID.fromString(ev.getReservationId());

        ReservationEntity reservation = inventoryReservationRepository.findById(reservationId)
                .orElseThrow(() -> new BusinessException(RESERVATION_NOT_FOUND));

        // ✅ Idempotent
        if (reservation.getStatus() == ReservationStatus.RELEASED
                || reservation.getStatus() == ReservationStatus.EXPIRED) {
            return;
        }

        if (reservation.getStatus() != ReservationStatus.ACTIVE) {
            throw new BusinessException(RESERVATION_STATUS_FAIL);
        }

        // 🔒 Lock stock theo batch
        List<String> skuCodes = reservation.getItems().stream()
                .map(ReservationItemEntity::getSkuCode)
                .sorted() // tránh deadlock
                .toList();

        Map<String, InventoryEntity> stockMap = inventoryRepository.findBySkuCodeInForUpdate(skuCodes)
                .stream()
                .collect(Collectors.toMap(InventoryEntity::getSkuCode, s -> s));

        for (ReservationItemEntity item : reservation.getItems()) {
            InventoryEntity stock = stockMap.get(item.getSkuCode());
            long qty = item.getQty();

            if (stock == null) {
                throw new BusinessException(STOCK_NOT_FOUND);
            }
            if (stock.getReservedStock() < qty) {
                throw new BusinessException(STOCK_RESERVED_INCONSISTENT);
            }

            stock.setReservedStock((int) (stock.getReservedStock() - qty)); // ✅ trả lại kho
        }

        inventoryRepository.saveAll(stockMap.values());

        reservation.setStatus(ReservationStatus.RELEASED);
        reservation.setUpdatedAt(LocalDateTime.now());
        inventoryReservationRepository.save(reservation);

        emitInventoryReleased(ev.getOrderId(), ev.getReservationId(), ev.getReason());
    }
    private void emitInventoryCommitted(String orderId, String reservationId) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of("orderId", orderId, "reservationId", reservationId)
            );

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(orderId)
                    .type("Inventory.Committed")
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    private void emitInventoryReleased(String orderId, String reservationId, String reason) {
        try {
            String payload = objectMapper.writeValueAsString(
                    Map.of(
                            "orderId", orderId,
                            "reservationId", reservationId,
                            "reason", reason
                    )
            );

            outboxRepository.save(OutboxEvent.builder()
                    .id(UUID.randomUUID().toString())
                    .aggregateType("inventory")
                    .aggregateId(orderId)
                    .type("Inventory.Released")
                    .payload(payload)
                    .createdAt(LocalDateTime.now())
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
