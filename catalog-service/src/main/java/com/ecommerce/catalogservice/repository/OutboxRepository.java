package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.OutboxEventEntity;
import com.ecommerce.catalogservice.entity.OutboxStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxRepository extends MongoRepository<OutboxEventEntity, String> {
    List<OutboxEventEntity> findTop50ByStatusAndNextRetryAtLessThanEqualOrderByCreatedAtAsc(
            OutboxStatus status, Instant now
    );
    List<OutboxEventEntity> findTop50ByStatusOrderByCreatedAtAsc(OutboxStatus status);
}
