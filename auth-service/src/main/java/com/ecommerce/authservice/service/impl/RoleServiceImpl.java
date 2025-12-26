package com.ecommerce.authservice.service.impl;

import static com.ecommerce.authservice.constant.Constants.*;

import com.ecommerce.authservice.dto.request.RoleCreateForm;
import com.ecommerce.authservice.dto.request.RoleEditForm;
import com.ecommerce.authservice.dto.response.BusinessException;
import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.dto.response.RoleDetailDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import com.ecommerce.authservice.repository.RoleRepository;
import com.ecommerce.authservice.repository.SubFunctionRepository;
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

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


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
        RoleEntity roleEntity = roleRepository.findById(id).orElseThrow(() -> new BusinessException(ROLE_NOT_EXIST));
        Set<String> subFunctions = new HashSet<>();
        if (roleEntity.getSubFunctions() != null) {
            subFunctions = roleEntity.getSubFunctions().stream().map(SubFunctionEntity::getId).collect(Collectors.toSet());
        }
        return RoleDetailDTO.builder()
                .id(roleEntity.getId())
                .name(roleEntity.getName())
                .description(roleEntity.getDescription())
                .subFunctions(subFunctions)
                .build();
    }

    @Override
    public void createRole(RoleCreateForm roleForm) {
        if (!StringUtils.hasText(roleForm.getName())
                || !StringUtils.hasText(roleForm.getDescription())
                || !StringUtils.hasText(roleForm.getId())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        boolean existed = roleRepository.existsById(roleForm.getId());
        if (existed) {
            throw new BusinessException(ROlE_EXIST);
        }
        Set<SubFunctionEntity> subFunctions = null;
        if (roleForm.getSubFunctions() != null && !roleForm.getSubFunctions().isEmpty()) {
            subFunctions = new HashSet<>(subFunctionRepository.findAllByIdIn(roleForm.getSubFunctions()));
            if (subFunctions.isEmpty() || subFunctions.size() != roleForm.getSubFunctions().size()) {
                throw new BusinessException(VALIDATE_FAIL);
            }
        }

        roleRepository.save(RoleEntity.builder()
                .id(roleForm.getId())
                .name(roleForm.getName())
                .description(roleForm.getDescription())
                .subFunctions(subFunctions)
                .build());
    }

    @Override
    public void updateRole(RoleEditForm roleForm, String id) {
        if (!StringUtils.hasText(roleForm.getName())
                || !StringUtils.hasText(roleForm.getDescription())
                || !StringUtils.hasText(roleForm.getOldId())
                || !StringUtils.hasText(roleForm.getNewId())
                || !id.equals(roleForm.getOldId())
        ) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        if (!roleForm.getOldId().equals(roleForm.getNewId()) && roleRepository.existsById(roleForm.getNewId()))
            throw new BusinessException(ROlE_EXIST);
        RoleEntity role = roleRepository.findById(roleForm.getOldId()).orElseThrow(() -> new BusinessException(ROLE_NOT_EXIST));
        if (roleForm.getSubFunctions() != null && !roleForm.getSubFunctions().isEmpty()) {
            Set<SubFunctionEntity> subFunctions = new HashSet<>(subFunctionRepository.findAllByIdIn(roleForm.getSubFunctions()));
            if (subFunctions.size() != roleForm.getSubFunctions().size()) throw new BusinessException(VALIDATE_FAIL);
            role.setSubFunctions(subFunctions);
        }
        role.setId(roleForm.getNewId());
        role.setName(roleForm.getName());
        role.setDescription(roleForm.getDescription());
        roleRepository.save(role);
    }

    @Override
    public void deleteRole(String id) {
        if (!StringUtils.hasText(id)) throw new BusinessException(VALIDATE_FAIL);
        RoleEntity role = roleRepository.findById(id).orElseThrow(() -> new BusinessException(ROLE_NOT_EXIST));
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
