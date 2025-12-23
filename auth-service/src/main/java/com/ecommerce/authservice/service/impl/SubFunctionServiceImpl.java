package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import com.ecommerce.authservice.repository.SubFunctionRepository;
import com.ecommerce.authservice.service.SubFunctionService;
import com.ecommerce.authservice.specs.SubFunctionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SubFunctionServiceImpl implements SubFunctionService {
    @Autowired
    SubFunctionRepository subFunctionRepository;

    @Override
    public Set<SubFunctionDTO> getUnlinkedSubFunctions() {
        Set<SubFunctionEntity> subFunctions = new HashSet<>(subFunctionRepository.findByFunctionIsNull());
        return subFunctions.stream().map(sf -> SubFunctionDTO.builder()
                .id(sf.getId())
                .name(sf.getName())
                .description(sf.getDescription())
                .sortOrder(sf.getSortOrder())
                .build()
        ).collect(Collectors.toSet());
    }

    @Override
    public Page<SubFunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        List<String> safeFields = normalizeFields(fields);

        // ✅ build spec
        Specification<SubFunctionEntity> spec =
                SubFunctionSpecification.keywordLike(keyword, safeFields);

        // ✅ build pageable (0-based page)
        Pageable pageable =
                PageRequest.of(Math.max(0, page), clampSize(size), parseSort(sort));

        // ✅ use pageable here
        return subFunctionRepository.findAll(spec, pageable)
                .map(sf -> SubFunctionDTO.builder()
                        .id(sf.getId())
                        .name(sf.getName())
                        .description(sf.getDescription())
                        .sortOrder(sf.getSortOrder())
                        .build());
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
        if (sort == null || sort.isBlank()) {
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
}
