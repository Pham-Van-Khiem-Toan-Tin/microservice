package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.entity.ProcessedEvent;
import com.ecommerce.orderservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessedEventStore {
    private final ProcessedEventRepository repo;

    public boolean tryMarkProcessed(String eventId) {
        try {
            repo.save(new ProcessedEvent(eventId, Instant.now()));
            return true; // lần đầu xử lý
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return false; // đã xử lý rồi
        }
    }
}
