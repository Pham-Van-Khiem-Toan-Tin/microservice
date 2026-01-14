package com.ecommerce.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticsearchProductService {

    private final ElasticsearchClient es;
    @Value("${app.es.index}") private String index;

    public void upsertProduct(String productId, Map<String, Object> doc) throws IOException {
        es.index(i -> i
                .index(index)
                .id(productId)
                .document(doc)
        );
    }

    public void deleteProduct(String productId) throws IOException {
        es.delete(d -> d.index(index).id(productId));
    }
}
