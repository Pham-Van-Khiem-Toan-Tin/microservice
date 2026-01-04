package com.ecommerce.catalogservice.service.impl;

import com.ecommerce.catalogservice.dto.request.ProductCreateForm;
import com.ecommerce.catalogservice.repository.CategoryRepository;
import com.ecommerce.catalogservice.repository.ProductRepository;
import com.ecommerce.catalogservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public void addProduct(ProductCreateForm form, MultipartFile image, List<MultipartFile> gallery) {

    }
//    @Autowired
//    private AttributeSetRepository attributeSetRepository;
}
