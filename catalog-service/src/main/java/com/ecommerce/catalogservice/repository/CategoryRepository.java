package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.CategoryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
    boolean existsByAttributeConfigsCode(String attributeConfigsCode);
    @Query(value = "{ 'attribute_configs': { $elemMatch: { 'code': ?0, 'allowed_option_ids': ?1 } } }", exists = true)
    boolean existsAttrConfigUsingOption(String code, String optionId);
    Optional<CategoryEntity> findBySlug(String slug);
//    List<CategoryEntity> findByMenuEnabledTrueOrderByMenuOrderAsc();
//    List<CategoryEntity> findByParentId(String parentId);

    Boolean existsBySlug(String slug);

    List<CategoryEntity> findAllByIsLeaf(Boolean isLeaf);
}
