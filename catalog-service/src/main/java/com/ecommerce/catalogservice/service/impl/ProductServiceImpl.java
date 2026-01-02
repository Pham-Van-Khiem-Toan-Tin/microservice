package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.repository.ProductRepository;
import com.ecommerce.catalogservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;
//    @Autowired
//    private AttributeSetRepository attributeSetRepository;
}
