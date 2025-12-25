package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.request.RoleForm;
import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import com.ecommerce.authservice.repository.RoleRepository;
import com.ecommerce.authservice.service.RoleService;
import com.ecommerce.authservice.specs.RoleSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;


@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;

    @Override
    public Page<RoleDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        List<String> safeFields = normalizeFields(fields);
        Specification<RoleEntity> spec =
                RoleSpecification.keywordLike(keyword, safeFields);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                clampSize(size),
                parseSort(sort)
        );
        return roleRepository.search(spec, pageable);
    }
    private static final Set<String> ALLOWED_SEARCH_FIELDS = Set.of("id", "name");
    private int clampSize(int size) {
        if (size <= 0) return 10;
        return Math.min(size, 100);
    }
    private List<String> normalizeFields(List<String> fields) {
        if (fields == null || fields.isEmpty()) return List.of("name"); // default
        return fields.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .filter(ALLOWED_SEARCH_FIELDS::contains)
                .distinct()
                .toList();
    }

    private Sort parseSort(String sort) {
        if (!StringUtils.hasText(sort)) {
            return Sort.by(Sort.Direction.DESC, "id");
        }

        String[] parts = sort.split(":");
        String field = parts[0].trim();                 // "id"
        String dir = parts.length > 1 ? parts[1].trim() : "asc"; // "desc"

        Sort.Direction direction =
                "desc".equalsIgnoreCase(dir) ? Sort.Direction.DESC : Sort.Direction.ASC;

        // Optional: whitelist field để tránh user truyền bậy
        Set<String> allowed = Set.of("id", "name", "sortOrder", "description");
        if (!allowed.contains(field)) field = "id";

        return Sort.by(direction, field);
    }
    @Override
    public void createRole(RoleForm roleForm) {
        if (!StringUtils.hasText(roleForm.getName())
                || !StringUtils.hasText(roleForm.getDescription())
                || !StringUtils.hasText(roleForm.getId())
        ) {
            throw new RuntimeException("Dữ liệu không hợp lệ");
        }
        boolean existed = roleRepository.existsById(roleForm.getId());
        if (!existed) {
            throw new RuntimeException("Quyền hạn đã tồn tại");
        }
        roleRepository.save(RoleEntity.builder()
                .id(roleForm.getId())
                .name(roleForm.getName())
                .description(roleForm.getDescription())
                .build());
    }
}
