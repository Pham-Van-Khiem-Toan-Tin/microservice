package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.CategoryEntity;
import org.bson.types.ObjectId;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import javax.swing.text.html.Option;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CategoryRepository extends MongoRepository<CategoryEntity, String> {
    Optional<CategoryEntity> findBySlug(String slug);
//    List<CategoryEntity> findByMenuEnabledTrueOrderByMenuOrderAsc();
//    List<CategoryEntity> findByParentId(String parentId);
    Boolean existsBySlug(String slug);

    List<CategoryEntity> findAllByIsLeaf(Boolean isLeaf);
}
