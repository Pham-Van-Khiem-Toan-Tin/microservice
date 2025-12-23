package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.repository.FunctionRepository;
import com.ecommerce.authservice.service.FunctionService;
import com.ecommerce.authservice.specs.FunctionSpecification;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FunctionServiceImpl implements FunctionService {
    @Autowired
    private FunctionRepository functionRepository;
    @Override
    public Set<AllFunctionDTO> findAllFunction() {
        Set<FunctionEntity> functions = new HashSet<>(functionRepository.findAllWithSubFunctions());
        return functions.stream().map((f) -> AllFunctionDTO.builder()
                .id(f.getId())
                .name(f.getName())
                .description(f.getDescription())
                .sortOrder(f.getSortOrder())
                .subFunctions(f.getSubFunctions().stream().map(sf ->
                        SubFunctionDTO.builder()
                                .id(sf.getId())
                                .name(sf.getName())
                                .description(sf.getDescription())
                                .sortOrder(sf.getSortOrder())
                                .build()).collect(Collectors.toSet()))
                .build()).collect(Collectors.toSet());
    }

    @Override
    public Page<FunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size) {
        List<String> safeFields = normalizeFields(fields);
        Specification<FunctionEntity> spec =
                FunctionSpecification.keywordLike(keyword, safeFields);
        Pageable pageable = PageRequest.of(
                Math.max(0, page),
                clampSize(size),
                parseSort(sort)
        );
        return functionRepository.search(spec, pageable);
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

}
