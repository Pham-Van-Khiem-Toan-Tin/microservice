package com.ecommerce.searchservice.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.Script;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.mapping.RuntimeFieldType;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.ecommerce.searchservice.dto.product.ProductListItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
public class ProductService {
    @Autowired
    private ElasticsearchClient client;
    public List<ProductListItem> getHotDeals(double minDiscount) throws IOException {

        SearchResponse<ProductListItem> response = client.search(s -> s
                        .index("products")
                        // 1. Định nghĩa "trường ảo" tính % giảm giá trên từng sản phẩm
                        .runtimeMappings("max_discount_rate", rm -> rm
                                .type(RuntimeFieldType.Double)
                                .script(sc -> sc.source(
                                        "if (params['_source']['skus'] == null) return; " +
                                                "for (item in params['_source']['skus']) { " +
                                                "  if (item.originalPrice != null && item.price != null && item.originalPrice > 0) { " +
                                                "    double rate = (double)(item.originalPrice - item.price) / item.originalPrice; " +
                                                "    emit(rate); " + // Trả giá trị vào trường ảo
                                                "  } " +
                                                "}"
                                ))
                        )
                        // 2. Truy vấn trên trường ảo đó
                        .query(q -> q
                                .range(r -> r
                                        .number(n -> n
                                            .field("max_discount_rate")
                                            .gte(minDiscount)
                                        )
                                )
                        )
                        .size(10),
                ProductListItem.class
        );

        return response.hits().hits().stream()
                .map(Hit::source)
                .collect(Collectors.toList());
    }
    public List<ProductListItem> getNewArrivals() throws IOException {
        SearchResponse<ProductListItem> response = client.search(s -> s
                        .index("products")
                        .query(q -> q.matchAll(m -> m)) // Lấy tất cả
                        .sort(sort -> sort
                                .field(f -> f.field("updatedAt").order(SortOrder.Desc))
                        )
                        .size(8), // Thường trang chủ chỉ cần 8-10 cái
                ProductListItem.class
        );
        return response.hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }
    public List<ProductListItem> getTopRated() throws IOException {
        return client.search(s -> s
                        .index("products")
                        .query(q -> q
                                .bool(b -> b
                                        // Ưu tiên sản phẩm có rating > 0
                                        .should(sh -> sh.range(r -> r.number(n -> n.field("averageRating").gt(0.0))))
                                        // Nếu không có, vẫn lấy các sản phẩm khác để lấp đầy khối
                                        .should(sh -> sh.matchAll(m -> m))
                                )
                        )
                        // Sắp xếp: Rating cao lên trước, sau đó đến số lượng review, cuối cùng là sản phẩm mới
                        .sort(sort -> sort.field(f -> f.field("averageRating").order(SortOrder.Desc)))
                        .sort(sort -> sort.field(f -> f.field("numberOfReviews").order(SortOrder.Desc)))
                        .sort(sort -> sort.field(f -> f.field("updatedAt").order(SortOrder.Desc)))
                        .size(8),
                ProductListItem.class
        ).hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }
    public List<ProductListItem> getProductsByPriceRange(double maxPrice) throws IOException {
        return client.search(s -> s
                        .index("products")
                        .query(q -> q.range(r -> r.number(n -> n.field("minPrice").lte(maxPrice))))
                        .size(8),
                ProductListItem.class
        ).hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }
    public List<ProductListItem> getRandomSuggestions() throws IOException {
        return client.search(s -> s
                        .index("products")
                        .query(q -> q.functionScore(fs -> fs.functions(f -> f.randomScore(rs -> rs))))
                        .size(8),
                ProductListItem.class
        ).hits().hits().stream().map(Hit::source).collect(Collectors.toList());
    }
}
