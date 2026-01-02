package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    Optional<ProductEntity> findBySlugAndCategoryId(String slug, String categoryId);

    Optional<ProductEntity> findBySlug(String slug);
}
