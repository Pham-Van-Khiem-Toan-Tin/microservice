package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.request.RoleForm;
import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.entity.RoleEntity;
import com.ecommerce.authservice.repository.RoleRepository;
import com.ecommerce.authservice.service.RoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;


@Service
public class RoleServiceImpl implements RoleService {
    @Autowired
    RoleRepository roleRepository;

    @Override
    public Page<RoleDTO> search(String keyword, Pageable pageable) {
        return roleRepository.search(keyword, pageable);
    }

    @Override
    public RoleEntity createRole(RoleForm roleForm) {
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
        return roleRepository.save(RoleEntity.builder()
                .id(roleForm.getId())
                .name(roleForm.getName())
                .description(roleForm.getDescription())
                .build());
    }
}
