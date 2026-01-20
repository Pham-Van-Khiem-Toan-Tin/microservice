package com.ecommerce.searchservice.utils;

import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.StringTermsBucket;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServiceUtils {
    private static final ObjectMapper MAPPER = new ObjectMapper();
    public static Map<String, Long> extractGlobalPriceRange(SearchResponse<Map> resp) {
        double min = 0.0;
        double max = 0.0;

        if (resp.aggregations() != null) {
            // 1. Lấy Global Aggregation
            var globalAgg = resp.aggregations().get("global_stats");

            // Kiểm tra null và đảm bảo đúng là kiểu Global
            if (globalAgg != null && globalAgg.global() != null) {

                // 2. Lấy Filter Aggregation (scope category)
                var catScope = globalAgg.global().aggregations().get("category_scope");

                if (catScope != null && catScope.filter() != null) {
                    var filterAgg = catScope.filter();

                    // 3. Lấy Min
                    var minRes = filterAgg.aggregations().get("min_price_all");
                    if (minRes != null && minRes.min() != null) {
                        min = minRes.min().value();
                    }

                    // 4. Lấy Max
                    var maxRes = filterAgg.aggregations().get("max_price_all");
                    if (maxRes != null && maxRes.max() != null) {
                        max = maxRes.max().value();
                    }
                }
            }
        }

        // Xử lý trường hợp không có dữ liệu (Elastic trả về Infinity)
        if (Double.isInfinite(min) || Double.isNaN(min)) min = 0;
        if (Double.isInfinite(max) || Double.isNaN(max)) max = 0;

        return Map.of(
                "min", (long) Math.floor(min),
                "max", (long) Math.ceil(max)
        );
    }
    public static List<Map<String, Object>> extractBrandsAgg(SearchResponse<Map> resp) {
        List<Map<String, Object>> brands = new ArrayList<>();
        if (resp.aggregations() == null) return brands;

        var agg = resp.aggregations().get("brands");
        if (agg == null) return brands;

        // ✅ case 1: string terms
        if (agg.sterms() != null) {
            var buckets = agg.sterms().buckets().array();
            if (buckets == null) return brands;

            for (var b : buckets) {
                Map<String, Object> brand = extractBrandFromBucketAggs(b.aggregations());
                if (brand == null) continue;

                Map<String, Object> out = new HashMap<>();
                out.put("id", brand.get("id"));          // hoặc b.key()
                out.put("name", brand.get("name"));
                out.put("slug", brand.get("slug"));
                out.put("count", b.docCount());
                brands.add(out);
            }
            return brands;
        }

        // ✅ case 2: long terms
        if (agg.lterms() != null) {
            var buckets = agg.lterms().buckets().array();
            if (buckets == null) return brands;

            for (var b : buckets) {
                Map<String, Object> brand = extractBrandFromBucketAggs(b.aggregations());
                if (brand == null) continue;

                Map<String, Object> out = new HashMap<>();
                out.put("id", brand.get("id"));          // hoặc String.valueOf(b.key())
                out.put("name", brand.get("name"));
                out.put("slug", brand.get("slug"));
                out.put("count", b.docCount());
                brands.add(out);
            }
            return brands;
        }

        // các loại khác: dterms (double), etc.
        return brands;
    }
    private static Map<String, Object> extractBrandFromBucketAggs(Map<String, Aggregate> subAggs) {
        if (subAggs == null) return null;

        var infoAgg = subAggs.get("info");
        if (infoAgg == null || infoAgg.topHits() == null) return null;

        var hits = infoAgg.topHits().hits().hits();
        if (hits == null || hits.isEmpty()) return null;

        Object srcObj = hits.get(0).source();
        if (srcObj == null) return null;

        // srcObj thường là JsonData/JacksonJsonBuffer
        Map<String, Object> srcMap;
        if (srcObj instanceof JsonData jd) {
            srcMap = jd.to(Map.class);
        } else {
            srcMap = MAPPER.convertValue(srcObj, Map.class);
        }

        if (srcMap == null) return null;
        Object brandObj = srcMap.get("brand");
        if (!(brandObj instanceof Map)) return null;

        return (Map<String, Object>) brandObj;
    }
}
