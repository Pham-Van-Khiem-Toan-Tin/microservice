package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.BrandEntity;
import com.ecommerce.catalogservice.entity.BrandStatus;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BrandRepository extends MongoRepository<BrandEntity, String> {
    Optional<BrandEntity> findByName(String name);

    boolean existsByNameOrSlug(String name, String slug);

    boolean existsByName(String name);

    boolean existsBySlug(String slug);

    List<BrandEntity> findAllByStatus(BrandStatus status);
}
