package com.ecommerce.productservice.service.impl;

import com.ecommerce.productservice.constants.ResponseCode;
import com.ecommerce.productservice.dto.request.CategoryForm;
import com.ecommerce.productservice.dto.response.ApiResponse;
import com.ecommerce.productservice.dto.response.BusinessException;
import com.ecommerce.productservice.entity.CategoryEntity;
import com.ecommerce.productservice.repository.CategoryRepository;
import com.ecommerce.productservice.service.CategoryService;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.text.Normalizer;
import java.util.Collections;
import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public List<CategoryEntity> findAllParent(String id) {
        Aggregation aggregation = Aggregation.newAggregation(
                Aggregation.match(Criteria.where("_id").is(id)),
                Aggregation.graphLookup("categories")
                        .startWith("$parentId")
                        .connectFrom("parentId")
                        .connectTo("_id")
                        .as("ancestors")
        );
        AggregationResults<Document> results =
                mongoTemplate.aggregate(aggregation, "categories", Document.class);

        Document doc = results.getUniqueMappedResult();
        if (doc == null) return List.of();

        List<Document> ancestors = (List<Document>) doc.get("ancestors");
        List<CategoryEntity> list = ancestors.stream()
                .map(d -> mongoTemplate.getConverter().read(CategoryEntity.class, d))
                .toList();

        Collections.reverse(list);
        return list;
    }

    @Override
    public CategoryEntity createCategory(CategoryForm form) {
        if (!StringUtils.hasText(form.getName()) || !StringUtils.hasText(form.getId())) throw new BusinessException(ResponseCode.DATA_FORMAT);
        String slug = toSlug(form.getName());
        boolean existedSlug = categoryRepository.existsBySlug(slug);
        if (existedSlug) throw new BusinessException(ResponseCode.SLUG_EXIST);
        CategoryEntity category = categoryRepository.save(CategoryEntity.builder()
                .id(form.getId())
                .name(form.getName())
                .slug(slug)
                .parentId(form.getParent())
                .build());
        if (category == null) throw new BusinessException(ResponseCode.INTERNAL_ERROR);
        return category;
    }

    @Override
    public CategoryEntity updateCategory() {
        return null;
    }

    @Override
    public CategoryEntity deleteCategory() {
        return null;
    }
    private String toSlug(String input) {
        if (input == null) return null;
        String slug = Normalizer.normalize(input, Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "") // bỏ dấu tiếng Việt
                .toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-");
        return slug;
    }
}
