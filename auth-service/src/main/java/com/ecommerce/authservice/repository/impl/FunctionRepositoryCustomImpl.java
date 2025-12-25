package com.ecommerce.authservice.repository.impl;

import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import com.ecommerce.authservice.repository.FunctionRepositoryCustom;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

@Repository
public class FunctionRepositoryCustomImpl implements FunctionRepositoryCustom {
    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<FunctionDTO> search(Specification<FunctionEntity> spec, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // =========================
        // 1) DATA QUERY
        // =========================
        CriteriaQuery<FunctionDTO> cq = cb.createQuery(FunctionDTO.class);
        Root<FunctionEntity> root = cq.from(FunctionEntity.class);

        Join<FunctionEntity, SubFunctionEntity> subJoin =
                root.join("subFunctions", JoinType.LEFT);

        Predicate where = (spec == null) ? cb.conjunction() : spec.toPredicate(root, cq, cb);
        cq.where(where);

        cq.groupBy(
                root.get("id"),
                root.get("name"),
                root.get("description"),
                root.get("sortOrder")
        );

        cq.select(cb.construct(
                FunctionDTO.class,
                root.get("id"),
                root.get("name"),
                root.get("description"),
                root.get("sortOrder"),
                root.get("icon"),
                cb.countDistinct(subJoin.get("id")) // quantityPermission
        ));

        // apply sort từ pageable (whitelist ở service để an toàn)
        if (pageable.getSort().isSorted()) {
            List<Order> orders = new ArrayList<>();
            for (Sort.Order o : pageable.getSort()) {
                Path<?> path = root.get(o.getProperty());
                orders.add(o.isAscending() ? cb.asc(path) : cb.desc(path));
            }
            cq.orderBy(orders);
        }

        TypedQuery<FunctionDTO> dataQuery = em.createQuery(cq);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<FunctionDTO> content = dataQuery.getResultList();

        // =========================
        // 2) COUNT QUERY (totalElements)
        // =========================
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<FunctionEntity> countRoot = countCq.from(FunctionEntity.class);

        Predicate countWhere = (spec == null) ? cb.conjunction() : spec.toPredicate(countRoot, countCq, cb);
        countCq.select(cb.countDistinct(countRoot.get("id")));
        countCq.where(countWhere);

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
