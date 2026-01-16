package com.ecommerce.searchservice.controller;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ecommerce.searchservice.dto.RelatedProductItem;
import com.ecommerce.searchservice.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
public class SearchController {
    @Autowired
    SearchService searchService;
    @GetMapping("/products")
    public Map<String, Object> searchProducts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) List<String> brandIds,
            @RequestParam(required = false) String categoryId,
            @RequestParam(required = false) String categoryAncestorId,
            @RequestParam(required = false) Long minPrice,
            @RequestParam(required = false) Long maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "relevance") String sort
    ) throws IOException {

        SearchResponse<Map> resp = searchService.searchProducts(
                q,
                brandIds,
                categoryId,
                categoryAncestorId,
                minPrice,
                maxPrice,
                page,
                size,
                sort
        );

        long total = resp.hits().total() != null ? resp.hits().total().value() : resp.hits().hits().size();

        return Map.of(
                "total", total,
                "page", page,
                "size", size,
                "items", resp.hits().hits().stream().map(h -> h.source()).toList()
        );
    }

    /**
     * ✅ Suggest (autocomplete)
     * مثال: /api/search/suggest?q=ni&limit=8
     */
    @GetMapping("/suggest")
    public SearchService.SuggestResult suggest(
            @RequestParam String q,
            @RequestParam(defaultValue = "8") int limit
    ) throws IOException {
        return searchService.suggest(q, limit);
    }
    @GetMapping("/related")
    public List<RelatedProductItem> related(
            @RequestParam String productId,
            @RequestParam(defaultValue = "8") int limit
    ) throws IOException {
        return searchService.relatedProducts(productId, limit);
    }
}
