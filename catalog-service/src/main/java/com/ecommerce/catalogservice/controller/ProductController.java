package com.ecommerce.catalogservice.controller;

import com.ecommerce.catalogservice.dto.request.category.CategorySearchField;
import com.ecommerce.catalogservice.dto.request.product.DiscontinuedForm;
import com.ecommerce.catalogservice.dto.request.product.ProductCreateForm;
import com.ecommerce.catalogservice.dto.request.product.ProductSearchField;
import com.ecommerce.catalogservice.dto.request.product.ProductUpdateForm;
import com.ecommerce.catalogservice.dto.response.ApiResponse;
import com.ecommerce.catalogservice.dto.response.product.ProductDTO;
import com.ecommerce.catalogservice.dto.response.product.ProductDetailDTO;
import com.ecommerce.catalogservice.dto.response.product.ProductPdpDTO;
import com.ecommerce.catalogservice.entity.SkuEntity;
import com.ecommerce.catalogservice.service.ProductService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ecommerce.catalogservice.constants.Constants.*;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    ProductService productService;

    @PreAuthorize("hasAuthority('VIEW_PRODUCT_LIST')")
    @GetMapping
    public Page<ProductDTO> findAll(
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(required = false, name = "fields") List<ProductSearchField> fields,
            @RequestParam(defaultValue = "name:asc") String sort,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size

    ) {
        Sort sortObj = parseSort(sort, Sort.by("id").ascending());
        Pageable pageable = PageRequest.of(page, size, sortObj);
        return productService.search(keyword, fields, pageable);
    }
    @GetMapping("/detail/{productId}")
    public ProductPdpDTO getProductDetail(@PathVariable String productId) {
        return productService.productDetail(productId);
    }
    @PreAuthorize("hasAuthority('VIEW_PRODUCT')")
    @GetMapping("/{id}")
    public ProductDetailDTO findById(@PathVariable String id) {
        return productService.productDetailDTO(id);
    }

    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
    @PostMapping
    public ApiResponse<Void> create(
            @ModelAttribute ProductCreateForm form,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey) throws JsonProcessingException {
        productService.addProduct(form, idempotencyKey);
        return ApiResponse.ok(PRODUCT_CREATE_SUCCESS);
    }

    @PreAuthorize("hasAuthority('DELETE_PRODUCT')")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) throws JsonProcessingException {
        productService.deleteProduct(id);
        return ApiResponse.ok(PRODUCT_CREATE_SUCCESS);
    }

    @PreAuthorize("hasAuthority('EDIT_PRODUCT')")
    @PutMapping("/{id}")
    public ApiResponse<Void> updateProduct(
            @PathVariable String id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @ModelAttribute ProductUpdateForm form) throws JsonProcessingException {
        productService.updateProduct(form, id, idempotencyKey);
        return ApiResponse.ok(PRODUCT_EDIT_SUCCESS);
    }

    @PreAuthorize("hasAuthority('EDIT_PRODUCT')")
    @PatchMapping("/sku/{id}/discontinued")
    public ApiResponse<String> discontinuedSku(
            @PathVariable String id,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @RequestBody DiscontinuedForm form) throws JsonProcessingException {
        String productId = productService.discontinuedSku(id, idempotencyKey, form);
        return ApiResponse.ok(PRODUCT_EDIT_SUCCESS, productId);
    }

    private Sort parseSort(String raw, Sort fallback) {
        try {
            if (raw == null || raw.isBlank()) return fallback;
            String[] parts = raw.split(":");
            String field = parts[0];
            Sort.Direction dir =
                    (parts.length > 1 && "desc".equalsIgnoreCase(parts[1]))
                            ? Sort.Direction.DESC
                            : Sort.Direction.ASC;
            return Sort.by(dir, field);
        } catch (Exception e) {
            return fallback;
        }
    }
}
