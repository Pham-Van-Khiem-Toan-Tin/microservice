package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.OutboxEventEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface OutboxRepository extends MongoRepository<OutboxEventEntity, ObjectId> {

}
