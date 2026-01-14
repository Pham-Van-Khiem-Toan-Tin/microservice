package com.ecommerce.inventoryservice.service.impl;

import com.ecommerce.inventoryservice.entity.ProcessedEvent;
import com.ecommerce.inventoryservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessedEventStore {

    private final ProcessedEventRepository repo;

    /**
     * @return true nếu event CHƯA xử lý (mark thành công),
     *         false nếu event đã xử lý trước đó
     */
    public boolean tryMarkProcessed(String eventId) {
        try {
            repo.saveAndFlush(new ProcessedEvent(eventId, Instant.now()));
            return true;
        } catch (DataIntegrityViolationException e) {
            return false;
        }
    }
}
