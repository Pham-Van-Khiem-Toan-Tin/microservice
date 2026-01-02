package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.AttributeEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeRepository extends MongoRepository<AttributeEntity, String> {
    Optional<AttributeEntity> findByCode(String code);
    boolean existsByCode(String code);
}
