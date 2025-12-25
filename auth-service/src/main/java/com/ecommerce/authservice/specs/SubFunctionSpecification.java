package com.ecommerce.authservice.specs;


import com.ecommerce.authservice.entity.SubFunctionEntity;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class SubFunctionSpecification {

    private SubFunctionSpecification() {}

    public static Specification<SubFunctionEntity> keywordLike(
            String keyword,
            List<String> fields
    ) {
        return (root, query, cb) -> {
            if (keyword == null || keyword.trim().isEmpty()) {
                return cb.conjunction(); // không filter gì
            }

            String kw = "%" + keyword.trim().toLowerCase() + "%";

            List<Predicate> predicates = new ArrayList<>();

            for (String field : fields) {
                switch (field) {
                    case "id" ->
                            predicates.add(
                                    cb.like(cb.lower(root.get("id").as(String.class)), kw)
                            );
                    case "name" ->
                            predicates.add(
                                    cb.like(cb.lower(root.get("name")), kw)
                            );
                }
            }

            return predicates.isEmpty()
                    ? cb.conjunction()
                    : cb.or(predicates.toArray(new Predicate[0]));
        };
    }
}
