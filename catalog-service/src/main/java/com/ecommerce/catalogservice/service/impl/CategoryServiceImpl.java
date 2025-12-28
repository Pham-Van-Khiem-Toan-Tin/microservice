package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.constants.Constants;
import com.ecommerce.catalogservice.dto.request.CategoryForm;
import com.ecommerce.catalogservice.dto.response.BusinessException;
import com.ecommerce.catalogservice.entity.CategoryEntity;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.service.CategoryService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;


    @Override
    public CategoryEntity createCategory(CategoryForm categoryForm) {
        CategoryEntity categoryEntity = new CategoryEntity();
        return categoryRepository.save(categoryEntity);
    }

    @Override
    public CategoryEntity updateCategory(CategoryForm categoryForm) {
        return null;
    }

    @Override
    public CategoryEntity deleteCategory(String id) {
        return null;
    }
}
