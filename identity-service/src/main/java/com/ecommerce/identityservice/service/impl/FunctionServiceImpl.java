package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.request.FunctionEditForm;
import com.ecommerce.identityservice.dto.request.FunctionForm;
import com.ecommerce.identityservice.dto.response.*;
import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.reppository.FunctionRepository;
import com.ecommerce.identityservice.reppository.SubFunctionRepository;
import com.ecommerce.identityservice.service.FunctionService;
import com.ecommerce.identityservice.specs.FunctionSpecification;
import jakarta.transaction.Transactional;
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

import static com.ecommerce.identityservice.constants.Constants.*;

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
                .id(f.getId().toString())
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
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        FunctionEntity functionEntity = functionRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(FUNCTION_NOT_EXIST)
        );
        Set<SubFunctionEntity> subFunctions = functionEntity.getSubFunctions();
        Set<SubFunctionList> subFunctionDTOSet = null;
        if (subFunctions != null) {
            subFunctionDTOSet = subFunctions.stream().map(sf -> SubFunctionList.builder()
                    .id(sf.getId())
                    .code(sf.getCode())
                    .name(sf.getName())
                    .description(sf.getDescription())
                    .build()).collect(Collectors.toSet());
        }
        return FunctionDetailDTO.builder()
                .id(functionEntity.getId())
                .code(functionEntity.getCode())
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
        if (!StringUtils.hasText(functionForm.getName())
                || !StringUtils.hasText(functionForm.getDescription())
                || !StringUtils.hasText(functionForm.getCode())
                || !StringUtils.hasText(functionForm.getIcon())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        FunctionEntity functionEntity = FunctionEntity.builder()
                .code(functionForm.getCode())
                .name(functionForm.getName())
                .description(functionForm.getDescription())
                .icon(functionForm.getIcon())
                .sortOrder(functionForm.getSortOrder())
                .build();
        if (functionForm.getSubFunctions() != null && !functionForm.getSubFunctions().isEmpty()) {
            Set<UUID> subFunctionIds = functionForm.getSubFunctions().stream().map(sf ->
                    UUID.fromString(sf.getId())).collect(Collectors.toSet());
            Set<SubFunctionEntity> subFunctionEntitySet = new HashSet<>(subFunctionRepository.findAllByIdIn(subFunctionIds));
            if (subFunctionEntitySet.isEmpty() || subFunctionEntitySet.size() != subFunctionIds.size())
                throw new BusinessException(VALIDATE_FAIL);
            subFunctionEntitySet.forEach(sf -> {sf.setFunction(functionEntity);});
            functionEntity.setSubFunctions(subFunctionEntitySet);
        }

        functionRepository.save(functionEntity);
    }

    @Override
    public void editFunction(FunctionEditForm functionForm, String id) {
        if (!StringUtils.hasText(functionForm.getName())
                || !StringUtils.hasText(functionForm.getDescription())
                || !StringUtils.hasText(functionForm.getCode())
                || !StringUtils.hasText(functionForm.getIcon())
                || !StringUtils.hasText(functionForm.getId())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        if (!functionForm.getId().equals(id))
            throw new BusinessException(VALIDATE_FAIL);
        FunctionEntity functionEntity = functionRepository.findById(UUID
                .fromString(functionForm.getId())).orElse(null);
        if (functionEntity == null)
            throw new BusinessException(FUNCTION_NOT_EXIST);
        Set<SubFunctionEntity> subFunctions = functionEntity.getSubFunctions();

        if ((functionForm.getSubFunctions() == null || functionForm.getSubFunctions()
                .isEmpty()) && subFunctions != null) {
            subFunctions.forEach(sf -> sf.setFunction(null));
            functionEntity.setSubFunctions(null);
        }
        if (functionForm.getSubFunctions() != null && !functionForm.getSubFunctions().isEmpty()) {
            Set<UUID> subFunctionIds = functionForm.getSubFunctions().stream().map(fs -> UUID.fromString(fs.getId())).collect(Collectors.toSet());
            Set<UUID> subFunctionIdsOfEntity = functionEntity.getSubFunctions().stream().map(SubFunctionEntity::getId).collect(Collectors.toSet());
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
        functionEntity.setCode(functionForm.getCode());
        functionEntity.setName(functionForm.getName());
        functionEntity.setDescription(functionForm.getDescription());
        functionEntity.setIcon(functionForm.getIcon());
        functionEntity.setSortOrder(functionForm.getSortOrder());
        functionRepository.save(functionEntity);

    }
    @Transactional
    @Override
    public void deleteFunction(String id) {
        if (!StringUtils.hasText(id))
            throw new BusinessException(VALIDATE_FAIL);
        FunctionEntity functionEntity = functionRepository.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(FUNCTION_NOT_EXIST)
        );
        subFunctionRepository.clearFunctionByFunctionId(functionEntity.getId());
        functionRepository.delete(functionEntity);
    }



    @Override
    public Set<FunctionDTO> findAllFunctionsOptions() {
        return functionRepository.findAll().stream().map(sf -> FunctionDTO.builder()
                .id(sf.getId())
                .code(sf.getCode())
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
