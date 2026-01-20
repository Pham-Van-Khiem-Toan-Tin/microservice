package com.ecommerce.searchservice.controller;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ecommerce.searchservice.dto.product.ProductListItem;
import com.ecommerce.searchservice.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/products")
public class ProductController {
    @Autowired
    private ProductService productService;
    @GetMapping("/hot-deals")
    public List<ProductListItem> hotDeals(
            @RequestParam(defaultValue = "0.0") double minDiscount
    ) throws IOException {

        // gọi service runtime-field hot deals
        return productService.getHotDeals(minDiscount);
    }
    @GetMapping("/top-rated")
    public List<ProductListItem> getTopRated() throws IOException {
        return productService.getTopRated();
    }

    // 3. Khối Sản phẩm theo phân khúc giá (Ví dụ: Dưới 15 triệu, 20 triệu...)
    @GetMapping("/price-range")
    public List<ProductListItem> getByPriceRange(
            @RequestParam(defaultValue = "20000000") double maxPrice
    ) throws IOException {
        return productService.getProductsByPriceRange(maxPrice);
    }

    // 4. Khối Gợi ý ngẫu nhiên (Discovery - Làm mới trang chủ mỗi lần load)
    @GetMapping("/suggestions")
    public List<ProductListItem> getRandomSuggestions() throws IOException {
        return productService.getRandomSuggestions();
    }
}
