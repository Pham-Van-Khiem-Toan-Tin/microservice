package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductVariantEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Map;
import java.util.Set;

public interface ProductVariantRepository extends MongoRepository<ProductVariantEntity, ObjectId> {
    boolean existsBySku(String sku);

    Set<ProductVariantEntity> findByProductId(ObjectId productId);

    Set<ProductVariantEntity> findByAttributesContaining(Map<String, String> attributes);
}
