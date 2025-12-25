package com.ecommerce.productservice.repository;

import com.ecommerce.productservice.entity.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
    boolean existsBySlug(String slug);
}
