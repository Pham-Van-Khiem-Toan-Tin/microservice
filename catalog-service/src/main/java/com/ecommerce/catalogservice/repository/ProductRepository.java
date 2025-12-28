package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, ObjectId> {
    Optional<ProductEntity> findBySlug(String slug);
    boolean existsBySlug(String slug);

    Set<ProductEntity> findByCategoryId(ObjectId categoryId);
}
