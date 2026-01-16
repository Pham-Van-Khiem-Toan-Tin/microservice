package com.ecommerce.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.Conflicts;
import co.elastic.clients.json.JsonData;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ElasticsearchProductService {

    private final ElasticsearchClient es;
    @Value("${app.es.index}")
    private String index;

    public void upsertProduct(String productId, Map<String, Object> doc) throws IOException {
        es.index(i -> i
                .index(index)
                .id(productId)
                .document(doc)
        );
    }

    public void updateBrandInProducts(String brandId, String brandName, String brandSlug) throws IOException {
        es.updateByQuery(u -> u
                .index(index)
                // ✅ tìm các product có brand.id = brandId
                .query(q -> q
                        .term(t -> t
                                .field("brand.id")   // nếu mapping là keyword thì ok
                                .value(brandId)
                        )
                )
                .script(sc -> sc
                        .lang("painless")
                        .source("""
                                    if (ctx._source.brand == null) { ctx._source.brand = new HashMap(); }
                                    ctx._source.brand.id = params.brandId;
                                    if (params.brandName != null) { ctx._source.brand.name = params.brandName; }
                                    if (params.brandSlug != null) { ctx._source.brand.slug = params.brandSlug; }
                                    ctx._source.updatedAt = params.updatedAt;
                                """)
                        .params(Map.of(
                                "brandId", JsonData.of(brandId),
                                "brandName", brandName == null ? JsonData.of((String) null) : JsonData.of(brandName),
                                "brandSlug", brandSlug == null ? JsonData.of((String) null) : JsonData.of(brandSlug),
                                "updatedAt", JsonData.of(Instant.now().toString())
                        ))
                )
                .conflicts(Conflicts.Proceed)
        );
    }
    public void updateCategoryInProducts(String categoryId, String name, String slug, List<String> ancestorIds) throws IOException {
        es.updateByQuery(u -> u
                .index(index)
                .query(q -> q
                        .term(t -> t
                                .field("category.id")   // nếu không match thử "category.id.keyword"
                                .value(categoryId)
                        )
                )
                .script(sc -> sc
                        .lang("painless")
                        .source("""
                if (ctx._source.category == null) { ctx._source.category = new HashMap(); }
                ctx._source.category.id = params.categoryId;
                if (params.name != null) { ctx._source.category.name = params.name; }
                if (params.slug != null) { ctx._source.category.slug = params.slug; }
                if (params.ancestorIds != null) { ctx._source.category.ancestorIds = params.ancestorIds; }
                ctx._source.updatedAt = params.updatedAt;
            """)
                        .params(Map.of(
                                "categoryId", JsonData.of(categoryId),
                                "name", name == null ? JsonData.of((String) null) : JsonData.of(name),
                                "slug", slug == null ? JsonData.of((String) null) : JsonData.of(slug),
                                "ancestorIds", ancestorIds == null ? JsonData.of((String) null) : JsonData.of(ancestorIds),
                                "updatedAt", JsonData.of(Instant.now().toString())
                        ))
                )
                .conflicts(Conflicts.Proceed)
        );
    }
    public void deleteProduct(String productId) throws IOException {
        es.delete(d -> d.index(index).id(productId));
    }
}
