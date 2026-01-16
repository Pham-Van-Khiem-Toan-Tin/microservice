package com.ecommerce.catalogservice.service;

import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.product.DiscontinuedForm;
import com.ecommerce.catalogservice.dto.request.product.ProductCreateForm;
import com.ecommerce.catalogservice.dto.request.product.ProductSearchField;
import com.ecommerce.catalogservice.dto.request.product.ProductUpdateForm;
import com.ecommerce.catalogservice.dto.response.product.ProductDTO;
import com.ecommerce.catalogservice.dto.response.product.ProductDetailDTO;
import com.ecommerce.catalogservice.dto.response.product.ProductPdpDTO;
import com.ecommerce.catalogservice.entity.SkuEntity;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ProductService {
    Page<ProductDTO> search(String keyword, List<ProductSearchField> fields, Pageable pageable);
    ProductDetailDTO productDetailDTO(String id);
    void addProduct(ProductCreateForm form, String idemKey) throws JsonProcessingException;
    void updateProduct(ProductUpdateForm form, String id, String idemKey) throws JsonProcessingException;
    void deleteProduct(String id);
    String discontinuedSku(String skuId, String idemKey, DiscontinuedForm form) throws JsonProcessingException;
    ProductPdpDTO productDetail(String id);
}
