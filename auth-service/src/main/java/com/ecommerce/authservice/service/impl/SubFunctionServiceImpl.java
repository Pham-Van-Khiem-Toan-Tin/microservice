package com.ecommerce.authservice.service.impl;

import static com.ecommerce.authservice.constant.Constants.*;

import com.ecommerce.authservice.dto.request.SubFunctionCreateForm;
import com.ecommerce.authservice.dto.request.SubFunctionEditForm;
import com.ecommerce.authservice.dto.request.SubFunctionForm;
import com.ecommerce.authservice.dto.request.SubFunctionOptionForm;
import com.ecommerce.authservice.dto.response.BusinessException;
import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import com.ecommerce.authservice.repository.SubFunctionRepository;
import com.ecommerce.authservice.service.SubFunctionService;
import com.ecommerce.authservice.specs.SubFunctionSpecification;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class SubFunctionServiceImpl implements SubFunctionService {
    @Autowired
    SubFunctionRepository subFunctionRepository;
    @Autowired
    private EntityManager entityManager;

    @Override
    public Set<SubFunctionDTO> getUnlinkedSubFunctions(SubFunctionOptionForm subFunctionOptionForm) {
        String keyword = StringUtils.hasText(subFunctionOptionForm.getKeyword()) ? subFunctionOptionForm.getKeyword() : "";
        Set<SubFunctionEntity> subFunctions = new HashSet<>(subFunctionRepository
                .searchAvailableByNameOrCode(keyword,
                        subFunctionOptionForm.getIds()
                                .stream()
                                .map(UUID::fromString)
                                .collect(Collectors.toSet())));
        return subFunctions.stream().map(sf -> SubFunctionDTO.builder()
                .id(sf.getId())
                .code(sf.getCode())
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
                        .code(sf.getCode())
                        .name(sf.getName())
                        .description(sf.getDescription())
                        .sortOrder(sf.getSortOrder())
                        .function(Optional.ofNullable(sf.getFunction())
                                .map(f -> FunctionDTO.builder()
                                        .id(f.getId())
                                        .code(f.getCode())
                                        .name(f.getName())
                                        .build())
                                .orElse(null))
                        .build());
    }


    @Override
    public SubFunctionEntity createSubFunction(SubFunctionCreateForm subFunctionForm) {
        if (!StringUtils.hasText(subFunctionForm.getName())
                || !StringUtils.hasText(subFunctionForm.getDescription())
                || !StringUtils.hasText(subFunctionForm.getCode()))
            throw new BusinessException(VALIDATE_FAIL);
        if (subFunctionRepository.existsByCode(subFunctionForm.getCode()))
            throw new BusinessException(SUBFUNCTION_EXIST);
        FunctionEntity functionEntity = null;
        if (StringUtils.hasText(subFunctionForm.getFunctionId())) {
            functionEntity = entityManager.find(FunctionEntity.class, UUID.fromString(subFunctionForm.getFunctionId()));
        }
        return subFunctionRepository.save(SubFunctionEntity.builder()
                .code(subFunctionForm.getCode())
                .name(subFunctionForm.getName())
                .description(subFunctionForm.getDescription())
                .sortOrder(1)
                .function(functionEntity)
                .build());
    }

    @Override
    public void updateSubFunction(SubFunctionEditForm subFunctionForm, String id) {
        if (!StringUtils.hasText(subFunctionForm.getName())
                || !StringUtils.hasText(subFunctionForm.getDescription())
                || !StringUtils.hasText(subFunctionForm.getCode())
                || !id.equals(subFunctionForm.getId()))
            throw new BusinessException(VALIDATE_FAIL);
        SubFunctionEntity subFunction = subFunctionRepository.findById(UUID.fromString(subFunctionForm.getId()))
                .orElseThrow(
                        () -> new BusinessException(SUBFUNCTION_NOT_EXIST)
                );
        FunctionEntity functionEntity = null;
        if (StringUtils.hasText(subFunctionForm.getFunctionId())) {
            functionEntity = entityManager.find(FunctionEntity.class, UUID.fromString(subFunctionForm.getFunctionId()));
        }
        subFunction.setCode(subFunctionForm.getCode());
        subFunction.setName(subFunctionForm.getName());
        subFunction.setDescription(subFunctionForm.getDescription());
        subFunction.setFunction(functionEntity);
        subFunctionRepository.save(subFunction);
    }

    @Override
    public SubFunctionDTO getSubFunction(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        SubFunctionEntity subFunction = subFunctionRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(SUBFUNCTION_NOT_EXIST)
        );
        FunctionEntity functionEntity = subFunction.getFunction();

        SubFunctionDTO subFunctionDTO = SubFunctionDTO.builder()
                .id(subFunction.getId())
                .code(subFunction.getCode())
                .name(subFunction.getName())
                .description(subFunction.getDescription())
                .build();
        if (functionEntity != null)
            subFunctionDTO.setFunction(FunctionDTO.builder()
                    .id(functionEntity.getId())
                    .code(functionEntity.getCode())
                    .name(functionEntity.getName())
                    .build());
        return subFunctionDTO;
    }

    @Override
    public void deleteSubFunction(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        SubFunctionEntity subFunction = subFunctionRepository.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException(SUBFUNCTION_NOT_EXIST));
        subFunctionRepository.delete(subFunction);
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
