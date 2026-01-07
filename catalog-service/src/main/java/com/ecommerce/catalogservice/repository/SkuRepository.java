package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.SkuEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SkuRepository extends MongoRepository<SkuEntity,String> {
}
