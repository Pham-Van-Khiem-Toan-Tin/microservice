package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.AttributeSetEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AttributeSetRepository extends MongoRepository<AttributeSetEntity, ObjectId> {
    Optional<AttributeSetEntity> findByCode(String code);
    boolean existsByCode(String code);
}
