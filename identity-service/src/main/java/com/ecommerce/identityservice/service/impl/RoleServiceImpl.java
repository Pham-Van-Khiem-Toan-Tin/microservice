package com.ecommerce.identityservice.service.impl;



import com.ecommerce.identityservice.dto.request.RoleCreateForm;
import com.ecommerce.identityservice.dto.request.RoleEditForm;
import com.ecommerce.identityservice.dto.response.BusinessException;
import com.ecommerce.identityservice.dto.response.RoleDTO;
import com.ecommerce.identityservice.dto.response.RoleDetailDTO;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.reppository.RoleRepository;
import com.ecommerce.identityservice.reppository.SubFunctionRepository;
import com.ecommerce.identityservice.service.RoleService;
import com.ecommerce.identityservice.specs.RoleSpecification;
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
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    private SubFunctionRepository subFunctionRepository;

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

    @Override
    public RoleDetailDTO findById(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(ROLE_NOT_EXIST);
        RoleEntity roleEntity = roleRepository.findById(UUID.fromString(id)).orElseThrow(() ->
                new BusinessException(ROLE_NOT_EXIST));
        Set<String> subFunctions = new HashSet<>();
        if (roleEntity.getSubFunctions() != null) {
            subFunctions = roleEntity.getSubFunctions()
                    .stream()
                    .map(sf -> sf.getId().toString())
                    .collect(Collectors.toSet());
        }
        return RoleDetailDTO.builder()
                .id(roleEntity.getId().toString())
                .code(roleEntity.getCode())
                .name(roleEntity.getName())
                .description(roleEntity.getDescription())
                .subFunctions(subFunctions)
                .build();
    }

    @Override
    public void createRole(RoleCreateForm roleForm) {
        if (!StringUtils.hasText(roleForm.getName())
                || !StringUtils.hasText(roleForm.getDescription())
                || !StringUtils.hasText(roleForm.getCode())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        boolean existed = roleRepository.existsByCode(roleForm.getCode());
        if (existed) {
            throw new BusinessException(ROlE_EXIST);
        }
        Set<SubFunctionEntity> subFunctions = null;
        if (roleForm.getSubFunctions() != null && !roleForm.getSubFunctions().isEmpty()) {
            subFunctions = new HashSet<>(subFunctionRepository.findAllByIdIn(roleForm.getSubFunctions().stream().map(UUID::fromString).collect(Collectors.toSet())));
            if (subFunctions.isEmpty() || subFunctions.size() != roleForm.getSubFunctions().size()) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        roleRepository.save(RoleEntity.builder()
                .code(roleForm.getCode())
                .name(roleForm.getName())
                .description(roleForm.getDescription())
                .subFunctions(subFunctions)
                .build());
    }

    @Override
    public void updateRole(RoleEditForm roleForm, String id) {
        if (!StringUtils.hasText(roleForm.getName())
                || !StringUtils.hasText(roleForm.getDescription())
                || !StringUtils.hasText(roleForm.getCode())
                || !StringUtils.hasText(roleForm.getId())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        if (!roleForm.getId().equals(id) && roleRepository.existsById(UUID.fromString(roleForm.getId())))
            throw new BusinessException(ROlE_EXIST);
        RoleEntity role = roleRepository.findById(UUID.fromString(roleForm.getId())).orElseThrow(() ->
                new BusinessException(ROLE_NOT_EXIST));
        if (roleForm.getSubFunctions() != null && !roleForm.getSubFunctions().isEmpty()) {
            Set<SubFunctionEntity> subFunctions = new HashSet<>(subFunctionRepository
                    .findAllByIdIn(roleForm.getSubFunctions()
                            .stream()
                            .map(UUID::fromString)
                            .collect(Collectors.toSet())));
            if (subFunctions.size() != roleForm.getSubFunctions().size()) throw new BusinessException(VALIDATE_FAIL);
            role.setSubFunctions(subFunctions);
        }
        role.setCode(roleForm.getCode());
        role.setName(roleForm.getName());
        role.setDescription(roleForm.getDescription());
        roleRepository.save(role);
    }

    @Override
    public void deleteRole(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(VALIDATE_FAIL);
        RoleEntity role = roleRepository.findById(UUID.fromString(id)).orElseThrow(() ->
                new BusinessException(ROLE_NOT_EXIST));
        roleRepository.delete(role);
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
