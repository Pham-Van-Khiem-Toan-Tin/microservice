package com.ecommerce.authservice.service.impl;

import static com.ecommerce.authservice.constant.Constants.*;

import com.ecommerce.authservice.dto.request.FunctionForm;
import com.ecommerce.authservice.dto.request.SubFunctionForm;
import com.ecommerce.authservice.dto.response.*;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import com.ecommerce.authservice.repository.FunctionRepository;
import com.ecommerce.authservice.repository.SubFunctionRepository;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class FunctionServiceImpl implements FunctionService {
    @Autowired
    private FunctionRepository functionRepository;
    @Autowired
    private SubFunctionRepository subFunctionRepository;

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
    public FunctionDetailDTO findFunctionById(String id) {
        FunctionEntity functionEntity = functionRepository.findById(id).orElseThrow(
                () -> new BusinessException(FUNCTION_NOT_EXIST)
        );
        Set<SubFunctionEntity> subFunctions = functionEntity.getSubFunctions();
        Set<SubFunctionList> subFunctionDTOSet = null;
        if (subFunctions != null) {
            subFunctionDTOSet = subFunctions.stream().map(sf -> SubFunctionList.builder()
                    .id(sf.getId())
                    .name(sf.getName())
                    .description(sf.getDescription())
                    .build()).collect(Collectors.toSet());
        }
        return FunctionDetailDTO.builder()
                .id(functionEntity.getId())
                .name(functionEntity.getName())
                .description(functionEntity.getDescription())
                .sortOrder(functionEntity.getSortOrder())
                .icon(functionEntity.getIcon())
                .subFunctions(subFunctionDTOSet)
                .build();
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

    @Override
    public void createFunction(FunctionForm functionForm) {
        validateSubFunctionForm(functionForm);
        Set<SubFunctionEntity> subFunctions = null;
        if (functionForm.getSubFunctions() != null && !functionForm.getSubFunctions().isEmpty()) {
            Set<String> subFunctionIds = functionForm.getSubFunctions().stream().map(SubFunctionForm::getId).collect(Collectors.toSet());
            Set<SubFunctionEntity> subFunctionEntitySet = new HashSet<>(subFunctionRepository.findAllByIdIn(subFunctionIds));
            if (subFunctionEntitySet.isEmpty() || subFunctionEntitySet.size() != subFunctionIds.size())
                throw new BusinessException(VALIDATE_FAIL);
            subFunctions = subFunctionEntitySet;
        }
        FunctionEntity functionEntity = FunctionEntity.builder()
                .id(functionForm.getId())
                .name(functionForm.getName())
                .description(functionForm.getDescription())
                .icon(functionForm.getIcon())
                .sortOrder(functionForm.getSortOrder())
                .subFunctions(subFunctions)
                .build();
        functionRepository.save(functionEntity);
    }

    @Override
    public void editFunction(FunctionForm functionForm, String id) {
        validateSubFunctionForm(functionForm);
        if (!functionForm.getId().equals(id))
            throw new BusinessException(VALIDATE_FAIL);
        FunctionEntity functionEntity = functionRepository.findById(functionForm.getId()).orElse(null);
        if (functionEntity == null)
            throw new BusinessException(FUNCTION_NOT_EXIST);
        Set<SubFunctionEntity> subFunctions = functionEntity.getSubFunctions();
        if ((functionForm.getSubFunctions() == null || functionForm.getSubFunctions().isEmpty()) && subFunctions != null) {
            subFunctions.forEach(sf -> sf.setFunction(null));
            functionEntity.setSubFunctions(null);
        }
        if (functionForm.getSubFunctions() != null && !functionForm.getSubFunctions().isEmpty()) {
            Set<String> subFunctionIds = functionForm.getSubFunctions().stream().map(SubFunctionForm::getId).collect(Collectors.toSet());
            Set<String> subFunctionIdsOfEntity = functionEntity.getSubFunctions().stream().map(SubFunctionEntity::getId).collect(Collectors.toSet());
            Set<SubFunctionEntity> subFunctionForm = new HashSet<>(subFunctionRepository.findAllByIdIn(subFunctionIds));
            if (subFunctionForm.isEmpty() || subFunctionForm.size() != subFunctionIds.size())
                throw new BusinessException(VALIDATE_FAIL);
            Set<SubFunctionEntity> subFunctionEntitySet = subFunctionForm.stream().filter(sf -> subFunctionIdsOfEntity.contains(sf.getId())).collect(Collectors.toSet());
            Set<SubFunctionEntity> subFunctionNotHaveParent = subFunctionForm.stream().filter(r -> r.getFunction() == null).collect(Collectors.toSet());
            if (subFunctionEntitySet.size() + subFunctionNotHaveParent.size() != subFunctionIds.size())
                throw new BusinessException(VALIDATE_FAIL);
            subFunctionForm.forEach(sf -> sf.setFunction(functionEntity));
            functionEntity.setSubFunctions(subFunctionForm);
        }
        functionEntity.setId(functionForm.getId());
        functionEntity.setName(functionForm.getName());
        functionEntity.setDescription(functionForm.getDescription());
        functionEntity.setIcon(functionForm.getIcon());
        functionEntity.setSortOrder(functionForm.getSortOrder());
        functionRepository.save(functionEntity);

    }

    @Override
    public void deleteFunction(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        FunctionEntity functionEntity = functionRepository.findById(id).orElseThrow(
                () -> new BusinessException(FUNCTION_NOT_EXIST)
        );
        functionRepository.delete(functionEntity);
    }

    private void validateSubFunctionForm(FunctionForm functionForm) {
        if (!StringUtils.hasText(functionForm.getName())
                || !StringUtils.hasText(functionForm.getDescription())
                || !StringUtils.hasText(functionForm.getId())
                || !StringUtils.hasText(functionForm.getIcon())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
    }

    @Override
    public Set<FunctionDTO> findAllFunctionsOptions() {
        return functionRepository.findAll().stream().map(sf -> FunctionDTO.builder()
                .id(sf.getId())
                .name(sf.getName())
                .build()).collect(Collectors.toSet());
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
