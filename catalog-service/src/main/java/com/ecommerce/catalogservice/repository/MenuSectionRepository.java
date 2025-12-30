package com.ecommerce.catalogservice.repository;

import com.ecommerce.catalogservice.entity.MenuSectionEntity;
import org.bson.types.ObjectId;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MenuSectionRepository extends MongoRepository<MenuSectionEntity, ObjectId> {
}
