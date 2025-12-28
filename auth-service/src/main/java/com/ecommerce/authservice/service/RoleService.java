package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.request.RoleCreateForm;
import com.ecommerce.authservice.dto.request.RoleEditForm;
import com.ecommerce.authservice.dto.response.RoleDTO;
import com.ecommerce.authservice.dto.response.RoleDetailDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {
    Page<RoleDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    RoleDetailDTO findById(String id);
    void createRole(RoleCreateForm roleForm);
    void updateRole(RoleEditForm roleForm, String id);
    void deleteRole(String id);
}
