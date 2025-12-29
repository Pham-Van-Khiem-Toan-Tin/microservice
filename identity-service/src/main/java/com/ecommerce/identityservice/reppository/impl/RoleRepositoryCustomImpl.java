package com.ecommerce.identityservice.reppository.impl;

import com.ecommerce.identityservice.dto.response.RoleDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.reppository.RoleRepositoryCustom;
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
public class RoleRepositoryCustomImpl implements RoleRepositoryCustom {
    @PersistenceContext
    private EntityManager em;
    @Override
    public Page<RoleDTO> search(Specification<RoleEntity> spec, Pageable pageable) {
        CriteriaBuilder cb = em.getCriteriaBuilder();

        // =========================
        // 1) DATA QUERY
        // =========================
        CriteriaQuery<RoleDTO> cq = cb.createQuery(RoleDTO.class);
        Root<RoleEntity> root = cq.from(RoleEntity.class);

        Join<RoleEntity, SubFunctionEntity> subJoin =
                root.join("subFunctions", JoinType.LEFT);

        Predicate where = (spec == null) ? cb.conjunction() : spec.toPredicate(root, cq, cb);
        cq.where(where);

        cq.groupBy(
                root.get("id"),
                root.get("code"),
                root.get("name"),
                root.get("description")
        );

        cq.select(cb.construct(
                RoleDTO.class,
                root.get("id"),
                root.get("code"),
                root.get("name"),
                root.get("description"),
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

        TypedQuery<RoleDTO> dataQuery = em.createQuery(cq);
        dataQuery.setFirstResult((int) pageable.getOffset());
        dataQuery.setMaxResults(pageable.getPageSize());
        List<RoleDTO> content = dataQuery.getResultList();

        // =========================
        // 2) COUNT QUERY (totalElements)
        // =========================
        CriteriaQuery<Long> countCq = cb.createQuery(Long.class);
        Root<RoleEntity> countRoot = countCq.from(RoleEntity.class);

        Predicate countWhere = (spec == null) ? cb.conjunction() : spec.toPredicate(countRoot, countCq, cb);
        countCq.select(cb.countDistinct(countRoot.get("id")));
        countCq.where(countWhere);

        Long total = em.createQuery(countCq).getSingleResult();

        return new PageImpl<>(content, pageable, total);
    }
}
