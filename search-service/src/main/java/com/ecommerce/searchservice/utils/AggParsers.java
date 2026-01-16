package com.ecommerce.searchservice.utils;

import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
import com.ecommerce.searchservice.service.SearchService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AggParsers {

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static List<SearchService.SuggestItem> parseTopHitAgg(
            SearchResponse<Map> resp,
            String aggName,
            String topHitName,
            String objName,          // "brand" hoặc "category"
            JsonpMapper mapper
    ) {
        if (resp == null || resp.aggregations() == null) return List.of();

        var agg = resp.aggregations().get(aggName);
        if (agg == null || agg.sterms() == null) return List.of();

        var buckets = agg.sterms().buckets();
        if (buckets == null || buckets.array() == null) return List.of();

        List<SearchService.SuggestItem> out = new ArrayList<>();

        for (var b : buckets.array()) {
            String id = b.key() != null ? b.key().stringValue() : null;
            if (id == null) continue;

            var sub = b.aggregations() != null ? b.aggregations().get(topHitName) : null;
            if (sub == null || sub.topHits() == null || sub.topHits().hits() == null
                    || sub.topHits().hits().hits() == null || sub.topHits().hits().hits().isEmpty()) {
                out.add(new SearchService.SuggestItem(id, id, null, null));
                continue;
            }

            JsonData srcJson = sub.topHits().hits().hits().get(0).source();
            if (srcJson == null) {
                out.add(new SearchService.SuggestItem(id, id, null, null));
                continue;
            }

            Map src = (Map) srcJson.to(Map.class, mapper);
            Object obj = src.get(objName);

            if (obj instanceof Map m) {
                String name = m.get("name") != null ? String.valueOf(m.get("name")) : id;
                String slug = m.get("slug") != null ? String.valueOf(m.get("slug")) : null;
                out.add(new SearchService.SuggestItem(id, name, slug, null));
            } else {
                out.add(new SearchService.SuggestItem(id, id, null, null));
            }
        }

        return out;
    }
}
