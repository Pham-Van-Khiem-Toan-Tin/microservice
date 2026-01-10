package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.SkuEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SkuRepository extends MongoRepository<SkuEntity, String> {
    List<SkuEntity> findAllBySpuId(String spuId);
}
