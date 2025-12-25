package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.request.RoleForm;
import com.ecommerce.authservice.dto.response.RoleDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {
    Page<RoleDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    void createRole(RoleForm roleForm);
}
