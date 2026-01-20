package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.ProductEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends MongoRepository<ProductEntity, String> {
    Optional<ProductEntity> findBySlugAndCategoryId(String slug, String categoryId);
    boolean existsBySpecsCode(String specsCode);
    Optional<ProductEntity> findBySlug(String slug);
    @Query(value = "{ 'specs.code': ?0 }", count = true)
    long countUsingSpecCode(String code);

    @Query(value = "{ 'specs.code': ?0 }", exists = true)
    boolean existsUsingSpecCode(String code);

    List<ProductEntity> findAllByIdIn(Collection<String> ids);
}
