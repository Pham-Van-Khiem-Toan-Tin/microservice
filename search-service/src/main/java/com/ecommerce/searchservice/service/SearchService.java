package com.ecommerce.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregation;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.ecommerce.searchservice.dto.RelatedProductItem;
import com.ecommerce.searchservice.dto.WishlistProductDto;
import com.ecommerce.searchservice.utils.AggParsers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SearchService {

    @Autowired
    ElasticsearchClient es;

    @Value("${app.es.index}")
    private String index;


    // ====== DTOs ======
    public record SuggestItem(String id, String label, String slug, String imageUrl) {
    }

    public record SkuSuggestItem(
            String skuId,
            String skuCode,

            String label,
            Long price,
            Long originalPrice,
            String imageUrl,
            String productId,
            String productSlug
    ) {
    }

    public record SuggestResult(
            List<SkuSuggestItem> skus,
            List<SuggestItem> brands,
            List<SuggestItem> categories
    ) {
    }

    // ✅ Search results (Enter)
    public SearchResponse<Map> searchProducts(
            String q, List<String> brandIds, String categoryId, String categoryAncestorId,
            Long minPrice, Long maxPrice,
            Double minRating, List<String> specs,
            int page, int size, String sort,
            Double minDiscount
    ) throws IOException {

        int from = Math.max(page, 0) * Math.max(size, 1);
        Query categoryQuery = Query.of(qu -> qu.bool(b -> {
            if (categoryId != null && !categoryId.isBlank()) {
                b.filter(f -> f.term(t -> t.field("category.id.keyword").value(categoryId)));
            }
            if (categoryAncestorId != null && !categoryAncestorId.isBlank()) {
                b.filter(f -> f.term(t -> t.field("category.ancestorIds.keyword").value(categoryAncestorId)));
            }
            return b;
        }));
        Query baseQuery = Query.of(qb -> qb.bool(b -> {
            if (q != null && !q.isBlank()) {
                String query = q.trim();
                b.must(m -> m.bool(bb -> bb
                        .minimumShouldMatch("1")
                        .should(sh -> sh.match(ma -> ma.field("name").query(query)))
                        .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("name").query(query)))
                        .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("brand.name").query(query)))
                        .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("category.name").query(query)))
                        .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("skus.name").query(query)))
                ));
            } else {
                b.must(m -> m.matchAll(ma -> ma));
            }

            if (categoryId != null && !categoryId.isBlank()) {
                b.filter(f -> f.term(t -> t.field("category.id.keyword").value(categoryId)));
            }
            if (categoryAncestorId != null && !categoryAncestorId.isBlank()) {
                b.filter(f -> f.term(t -> t.field("category.ancestorIds.keyword").value(categoryAncestorId)));
            }
            return b;
        }));

        Query postFilterQuery = Query.of(qf -> qf.bool(b -> {

            if (minRating != null && minRating > 0) {
                b.filter(f -> f.range(r -> r.number(n -> n.field("averageRating").gte(minRating))));
            }

            if (specs != null && !specs.isEmpty()) {
                for (String spec : specs) {
                    String[] parts = spec.split(":");
                    if (parts.length < 2) continue;
                    String key = parts[0];
                    String value = parts[1];

                    b.filter(f -> f.bool(bool -> bool
                            .must(m -> m.term(t -> t.field("specs.code.keyword").value(key)))
                            .must(m -> m.bool(sub -> sub
                                    .should(sh -> sh.term(t -> t.field("specs.value.keyword").value(value)))
                                    .should(sh -> sh.term(t -> t.field("specs.valueId.keyword").value(value)))
                            ))
                    ));
                }
            }

            if (minPrice != null || maxPrice != null) {
                b.filter(f -> f.range(r -> r.number(n -> n.field("minPrice")
                        .gte(minPrice != null ? minPrice.doubleValue() : null)
                        .lte(maxPrice != null ? maxPrice.doubleValue() : null))));
            }

            if (brandIds != null) {
                var cleaned = brandIds.stream().filter(x -> x != null && !x.isBlank()).toList();
                if (!cleaned.isEmpty()) {
                    b.filter(f -> f.terms(t -> t.field("brand.id.keyword")
                            .terms(ts -> ts.value(cleaned.stream().map(FieldValue::of).toList()))
                    ));
                }
            }
            if (minDiscount != null && minDiscount > 0) {
                b.filter(f -> f.range(r -> r.number(n -> n
                        .field("max_discount_rate")
                        .gte(minDiscount)
                )));
            }
            return b;
        }));
        boolean needDiscountRuntime =
                (minDiscount != null && minDiscount > 0)
                        || "discount_desc".equalsIgnoreCase(sort);
        return es.search(s -> {
            s.index(index);
            s.from(from).size(size);
            s.query(baseQuery);
            s.postFilter(postFilterQuery);
            // 2. POST FILTER: Chỉ dùng để lọc BrandIds
            // Cái này chạy SAU khi Aggregation đã tính toán xong
            if (needDiscountRuntime) {
                s.runtimeMappings("max_discount_rate", rm -> rm
                        .type(co.elastic.clients.elasticsearch._types.mapping.RuntimeFieldType.Double)
                        .script(sc -> sc.source(
                                "if (params._source == null || params._source.skus == null) return; " +
                                        "double best = 0.0; " +
                                        "for (def item : params._source.skus) { " +
                                        "  if (item == null) continue; " +
                                        "  def op = item.originalPrice; def p = item.price; " +
                                        "  if (op != null && p != null && op > 0 && op > p) { " +
                                        "    double rate = (double)(op - p) / (double)op; " +
                                        "    if (rate > best) best = rate; " +
                                        "  } " +
                                        "} " +
                                        "emit(best);"
                        ))
                );
            }
            // 3. SOURCE FILTER (Dữ liệu trả về cho client)
            s.source(src -> src.filter(f -> f.includes(List.of(
                    "name", "slug", "thumbnail.url", "productId", "numberOfReviews", "averageRating",
                    "minPrice", "maxPrice", "brand.id", "brand.name", "brand.slug",
                    "category.id", "category.name", "category.slug",
                    "variantGroups", "skus"
            ))));
            s.aggregations("global_stats", a -> a
                    .global(g -> g) // 1. Global: Reset context về toàn bộ documents trong index
                    .aggregations("category_scope", sub -> sub
                            .filter(categoryQuery) // 2. Filter: Chỉ giữ lại documents thuộc Category hiện tại
                            .aggregations("min_price_all", m -> m.min(mn -> mn.field("minPrice"))) // 3. Tính Min
                            .aggregations("max_price_all", m -> m.max(mx -> mx.field("maxPrice"))) // 4. Tính Max
                    )
            );
            s.aggregations("brands", a -> a
                    .terms(t -> t.field("brand.id.keyword").size(200))
                    .aggregations("info", aa -> aa
                            .topHits(th -> th.size(1)
                                    .source(src -> src.filter(f -> f.includes(List.of("brand.id", "brand.name", "brand.slug")))))
                    )
            );

            // 5. SORT
            if ("price_asc".equalsIgnoreCase(sort)) {
                s.sort(so -> so.field(f -> f.field("minPrice").order(SortOrder.Asc)));
            } else if ("price_desc".equalsIgnoreCase(sort)) {
                s.sort(so -> so.field(f -> f.field("minPrice").order(SortOrder.Desc)));
            } else if ("newest".equalsIgnoreCase(sort)) {
                s.sort(so -> so.field(f -> f.field("updatedAt").order(SortOrder.Desc)));
            } else if ("rating_desc".equalsIgnoreCase(sort)) {
                s.sort(so -> so.field(f -> f.field("averageRating").order(SortOrder.Desc)));
                s.sort(so -> so.field(f -> f.field("numberOfReviews").order(SortOrder.Desc)));
            } else if ("discount_desc".equalsIgnoreCase(sort)) {
                // sort theo runtime field giảm giá
                s.sort(so -> so.field(f -> f.field("max_discount_rate").order(SortOrder.Desc)));
            } else {
                // relevance mặc định: ES score
                s.sort(so -> so.score(sc -> sc.order(SortOrder.Desc)));
            }

            return s;
        }, Map.class);
    }


    // ✅ Suggest khi gõ (trả SKU + brand + category)
    public SuggestResult suggest(String q, int limit) throws IOException {
        String query = (q == null) ? "" : q.trim();
        if (query.length() < 2) {
            return new SuggestResult(List.of(), List.of(), List.of());
        }

        int size = Math.min(Math.max(limit, 1), 10);

        SearchResponse<Map> resp = es.search(s -> {
            s.index(index);
            s.size(size);

            s.source(src -> src.filter(f -> f.includes(List.of(
                    "name", "slug", "productId",
                    "thumbnail.url",
                    "skus.skuId", "skus.skuCode", "skus.name", "skus.price", "skus.originalPrice", "skus.thumbnail.url",
                    "skus.originalPrice",
                    "brand.id", "brand.name", "brand.slug",
                    "category.id", "category.name", "category.slug"
            ))));

            // match keyword vào name + sku.name + skuCode + brand + category
            s.query(qb -> qb.bool(b -> b
                    .minimumShouldMatch("1")

                    // PRODUCT NAME
                    .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("name").query(query)))
                    .should(sh -> sh.prefix(p -> p.field("name.keyword").value(query).caseInsensitive(true)))

                    // SKU NAME
                    .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("skus.name").query(query)))
                    .should(sh -> sh.prefix(p -> p.field("skus.name.keyword").value(query).caseInsensitive(true)))

                    // SKU CODE
                    .should(sh -> sh.prefix(p -> p.field("skus.skuCode.keyword").value(query).caseInsensitive(true)))

                    // BRAND
                    .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("brand.name").query(query)))
                    .should(sh -> sh.prefix(p -> p.field("brand.name.keyword").value(query).caseInsensitive(true)))

                    // CATEGORY
                    .should(sh -> sh.matchPhrasePrefix(mpp -> mpp.field("category.name").query(query)))
                    .should(sh -> sh.prefix(p -> p.field("category.name.keyword").value(query).caseInsensitive(true)))
            ));

            // aggs
            s.aggregations("brands", Aggregation.of(a -> a
                    .terms(t -> t.field("brand.id.keyword").size(size))
                    .aggregations("top_brand", Aggregation.of(aa -> aa
                            .topHits(th -> th.size(1)
                                    .source(ss -> ss.filter(ff -> ff.includes(List.of(
                                            "brand.id", "brand.name", "brand.slug"
                                    ))))
                            )
                    ))
            ));

            s.aggregations("categories", Aggregation.of(a -> a
                    .terms(t -> t.field("category.id.keyword").size(size))
                    .aggregations("top_cat", Aggregation.of(aa -> aa
                            .topHits(th -> th.size(1)
                                    .source(ss -> ss.filter(ff -> ff.includes(List.of(
                                            "category.id", "category.name", "category.slug"
                                    ))))
                            )
                    ))
            ));

            return s;
        }, Map.class);

        // ✅ build SKU list từ hits (Cách 1: không cần nested)
        String qLower = query.toLowerCase();

        List<SkuSuggestItem> skus = resp.hits().hits().stream()
                .filter(h -> h.source() != null)
                .flatMap(h -> {
                    Map src = h.source();

                    String productId = src.get("productId") != null ? String.valueOf(src.get("productId")) : h.id();
                    String productName = String.valueOf(src.getOrDefault("name", ""));
                    String productSlug = src.get("slug") != null ? String.valueOf(src.get("slug")) : null;

                    Object skusObj = src.get("skus");
                    if (!(skusObj instanceof List<?> skuList)) return java.util.stream.Stream.empty();

                    // fallback ảnh product
                    String tmpThumb = null;
                    Object prodThumbObj = src.get("thumbnail");
                    if (prodThumbObj instanceof Map<?, ?> tm && tm.get("url") != null) {
                        tmpThumb = String.valueOf(tm.get("url"));
                    }

                    final String productThumb = tmpThumb;
                    return skuList.stream()
                            .filter(o -> o instanceof Map)
                            .map(o -> (Map) o)
                            .filter(sku -> {
                                String skuName = sku.get("name") != null ? String.valueOf(sku.get("name")).toLowerCase() : "";
                                String skuCode = sku.get("skuCode") != null ? String.valueOf(sku.get("skuCode")).toLowerCase() : "";
                                // match nhẹ để ưu tiên sku liên quan
                                return skuName.contains(qLower) || skuCode.contains(qLower) || productName.toLowerCase().contains(qLower);
                            })
                            .map(sku -> {
                                String skuId = sku.get("skuId") != null ? String.valueOf(sku.get("skuId")) : null;
                                String skuCode = sku.get("skuCode") != null ? String.valueOf(sku.get("skuCode")) : null;
                                String skuName = sku.get("name") != null ? String.valueOf(sku.get("name")) : "";
                                String imageUrl = null;
                                Long price = null;
                                Long originalPrice = null;
                                Object priceObj = sku.get("price");
                                Object originalPriceObj = sku.get("originalPrice");
                                if (priceObj instanceof Number n) price = n.longValue();
                                if (originalPriceObj instanceof Number n) originalPrice = n.longValue();
                                Object skuThumbObj = sku.get("thumbnail");
                                if (skuThumbObj instanceof Map<?, ?> stm && stm.get("url") != null) {
                                    imageUrl = String.valueOf(stm.get("url"));
                                }

                                // ✅ dùng productThumb thoải mái
                                if (imageUrl == null) {
                                    imageUrl = productThumb;
                                }

                                return new SkuSuggestItem(
                                        skuId,
                                        skuCode,
                                        skuName,
                                        price,
                                        originalPrice,
                                        imageUrl,
                                        productId,
                                        productSlug
                                );
                            });
                })
                .limit(size)
                .collect(Collectors.toList());

        var mapper = es._transport().jsonpMapper();
        List<SuggestItem> brands = AggParsers.parseTopHitAgg(resp, "brands", "top_brand", "brand", mapper);
        List<SuggestItem> categories = AggParsers.parseTopHitAgg(resp, "categories", "top_cat", "category", mapper);

        return new SuggestResult(skus, brands, categories);
    }

    public List<RelatedProductItem> relatedProducts(String productId, int limit) throws IOException {
        int size = Math.min(Math.max(limit, 1), 12);

        // 1) Lấy doc hiện tại trong ES
        var getResp = es.get(g -> g.index(index).id(productId), Map.class);
        if (!getResp.found() || getResp.source() == null) return List.of();

        Map src0 = getResp.source();
        String esDocId = null;
        // 2) Extract categoryId / brandId / minPrice từ source
        if (getResp.found() && getResp.source() != null) {
            src0 = getResp.source();
            esDocId = getResp.id(); // _id
        } else {
            var sr = es.search(s -> s.index(index).size(1)
                            .query(q -> q.term(t -> t.field("productId.keyword").value(productId)))
                    , Map.class);
            var hit = sr.hits().hits().stream().findFirst().orElse(null);
            if (hit == null || hit.source() == null) return List.of();
            src0 = hit.source();
            esDocId = hit.id();
        }

        // Extract
        String categoryId = null;
        Object catObj = src0.get("category");
        if (catObj instanceof Map<?, ?> cm && cm.get("id") != null) categoryId = String.valueOf(cm.get("id"));

        String brandId = null;
        Object brandObj = src0.get("brand");
        if (brandObj instanceof Map<?, ?> bm && bm.get("id") != null) brandId = String.valueOf(bm.get("id"));

        Double gte = null, lte = null;
        Object mp = src0.get("minPrice");
        if (mp instanceof Number n && n.doubleValue() > 0) {
            gte = n.doubleValue() * 0.7;
            lte = n.doubleValue() * 1.3;
        }
        final String finalCategoryId = categoryId;
        final String finalBrandId = brandId;
        final Double finalGte = gte;
        final Double finalLte = lte;
        final String finalEsDocId = esDocId;      // _id thật sự
        final String finalProductId = productId;
        // 3) Query related
        SearchResponse<Map> resp = es.search(s -> {
            s.index(index).size(size);

            s.source(src -> src.filter(f -> f.includes(List.of(
                    "productId", "name", "slug", "thumbnail.url",
                    "minPrice", "maxPrice",
                    "brand.name", "brand.slug",
                    "averageRating", "numberOfReviews",
                    "variantGroups", "skus"
            ))));

            s.query(qb -> qb.bool(b -> {
                // ✅ exclude self: cả _id và field productId
                if (finalEsDocId != null) {
                    b.mustNot(mn -> mn.ids(i -> i.values(finalEsDocId)));
                }
                b.mustNot(mn -> mn.term(t -> t.field("productId.keyword").value(finalProductId)));

                if (finalCategoryId != null) {
                    b.filter(f -> f.term(t -> t.field("category.id.keyword").value(finalCategoryId)));
                }

                if (finalGte != null || finalLte != null) {
                    b.filter(f -> f.range(r -> r.number(nn -> nn
                            .field("minPrice")
                            .gte(finalGte)
                            .lte(finalLte)
                    )));
                }

                if (finalBrandId != null) {
                    b.should(sh -> sh.term(t -> t.field("brand.id.keyword").value(finalBrandId)));
                }

                b.minimumShouldMatch("0");
                return b;
            }));

            s.sort(so -> so.score(sc -> sc.order(SortOrder.Desc)));
            s.sort(so -> so.field(f -> f.field("updatedAt").order(SortOrder.Desc)));
            return s;
        }, Map.class);

        return resp.hits().hits().stream()
                .filter(h -> h.source() != null)
                .filter(h -> {
                    Map src = h.source();
                    String pid = src.get("productId") != null ? String.valueOf(src.get("productId")) : null;
                    if (pid != null) return !pid.equals(finalProductId);
                    return finalEsDocId == null || !h.id().equals(finalEsDocId);
                })
                .map(h -> {
                    Map src = h.source();
                    String id = h.id(); // hoặc src.get("productId")
                    String name = String.valueOf(src.getOrDefault("name", ""));
                    String slug = String.valueOf(src.getOrDefault("slug", ""));
                    String imageUrl = null;
                    Object thumbObj = src.get("thumbnail");
                    if (thumbObj instanceof Map<?, ?> tm && tm.get("url") != null) {
                        imageUrl = String.valueOf(tm.get("url"));
                    }
                    Long minPrice = (src.get("minPrice") instanceof Number n) ? n.longValue() : null;
                    Long maxPrice = (src.get("maxPrice") instanceof Number n) ? n.longValue() : null;
                    Double rating = 0.0;
                    if (src.get("averageRating") instanceof Number n) {
                        rating = n.doubleValue();
                    }

                    Integer ratingCount = 0;
                    if (src.get("numberOfReviews") instanceof Number n) {
                        ratingCount = n.intValue();
                    }
                    String brandName = null, brandSlug = null;
                    Object bObj = src.get("brand");
                    if (bObj instanceof Map<?, ?> bm) {
                        if (bm.get("name") != null) brandName = String.valueOf(bm.get("name"));
                        if (bm.get("slug") != null) brandSlug = String.valueOf(bm.get("slug"));
                    }
                    List<Map<String, Object>> variantGroups = null;
                    if (src.get("variantGroups") instanceof List<?>) {
                        variantGroups = (List<Map<String, Object>>) src.get("variantGroups");
                    }

                    List<Map<String, Object>> skus = null;
                    if (src.get("skus") instanceof List<?>) {
                        skus = (List<Map<String, Object>>) src.get("skus");
                    }
                    return new RelatedProductItem(id, name, slug, imageUrl, minPrice, maxPrice, brandName, brandSlug, rating, ratingCount, variantGroups, skus);
                })
                .toList();
    }

    public List<Map<String, Object>> getProductsByIds(List<String> productIds) throws IOException {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }

        SearchResponse<Map> response = es.search(s -> {
            s.index(index);
            s.size(productIds.size());

            // Lấy Full các trường theo yêu cầu Frontend
            s.source(src -> src.filter(f -> f.includes(List.of(
                    "productId", "name", "slug",
                    "numberOfReviews", "averageRating",
                    "thumbnail", // Lấy cả object thumbnail
                    "minPrice", "maxPrice",
                    "brand", "category",
                    "variantGroups", "skus"
            ))));

            // Query theo ID
            s.query(q -> q.terms(t -> t
                    .field("productId.keyword")
                    .terms(ts -> ts.value(productIds.stream().map(FieldValue::of).toList()))
            ));

            return s;
        }, Map.class);

        // Trả về Source Map trực tiếp
        return response.hits().hits().stream()
                .filter(h -> h.source() != null)
                .map(h -> {
                    Map<String, Object> source = h.source();
                    // Đảm bảo productId luôn có (nếu trong source k có thì lấy từ _id)
                    if (!source.containsKey("productId")) {
                        source.put("productId", h.id());
                    }
                    return source;
                })
                .collect(Collectors.toList());
    }

}
