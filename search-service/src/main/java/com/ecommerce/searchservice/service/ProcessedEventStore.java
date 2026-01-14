package com.ecommerce.searchservice.service;

import com.ecommerce.searchservice.entity.ProcessedEvent;
import com.ecommerce.searchservice.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ProcessedEventStore {
    private final ProcessedEventRepository repo;

    public boolean tryMarkProcessed(String eventId) {
        try {
            repo.insert(new ProcessedEvent(eventId, Instant.now()));
            return true; // lần đầu xử lý
        } catch (org.springframework.dao.DuplicateKeyException e) {
            return false; // đã xử lý rồi
        }
    }
}
