package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.AttributeEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttributeRepository extends MongoRepository<AttributeEntity, String> {
    List<AttributeEntity> findAllByCodeIn(Collection<String> codes);
    boolean existsByCode(String code);

    List<AttributeEntity> findByCodeIn(Collection<String> codes);

    List<AttributeEntity> findAllByCode(String code);
}
