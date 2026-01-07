package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    Optional<ProductEntity> findBySlugAndCategoryId(String slug, String categoryId);
    boolean existsBySpecsCode(String specsCode);
    Optional<ProductEntity> findBySlug(String slug);
}
