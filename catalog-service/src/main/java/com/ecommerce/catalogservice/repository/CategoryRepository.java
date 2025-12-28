package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.CategoryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findBySlugAndParentId(String slug, ObjectId parentId);
    Set<CategoryEntity> findByParentIdAndIsVisibleOrderBySortOrderAsc(ObjectId parentId, Boolean isVisible);
    Set<CategoryEntity> findByIsVisibleOrderBySortOrderAsc(Boolean isVisible);
    boolean existsBySlug(String slug);
}
