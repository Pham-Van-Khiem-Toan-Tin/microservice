package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.BrandEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<BrandEntity, String> {
    Optional<BrandEntity> findByCode(String code);
}
