//package com.ecommerce.catalogservice.controller;
//
//import com.ecommerce.catalogservice.dto.request.product.ProductCreateForm;
//import com.ecommerce.catalogservice.dto.response.ApiResponse;
//import com.ecommerce.catalogservice.service.ProductService;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.ModelAttribute;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import static com.ecommerce.catalogservice.constants.Constants.*;
//
//@RestController
//@RequestMapping("/products")
//public class ProductController {
//    @Autowired
//    ProductService productService;
//    @PreAuthorize("hasAuthority('CREATE_PRODUCT')")
//    @PostMapping
//    public ApiResponse<Void> create(@ModelAttribute ProductCreateForm form) {
//        productService.addProduct(form);
//        return ApiResponse.ok(PRODUCT_CREATE_SUCCESS);
//    }
//}
