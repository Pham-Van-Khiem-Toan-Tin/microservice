package com.ecommerce.orderservice.specs;

import com.ecommerce.orderservice.entity.OrderEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class OrderSpecification {
    private void OrderSpecifications() {}

    public static Specification<OrderEntity> keywordLike(String keyword,
                                                      List<String> fields) {
        return (root, query, cb) -> {
            // tránh duplicate rows khi join orderItems
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // không filter gì
            }
            String kw = "%" + keyword.trim().toLowerCase() + "%";
            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                switch (field) {
                    case "orderNumber" ->
                            predicates.add(
                                    cb.like(cb.lower(root.get("orderNumber")), kw)
                            );
                    case "userId" ->
                            predicates.add(
                                    cb.like(cb.lower(root.get("userId")), kw)
                            );
                }
            }



            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
