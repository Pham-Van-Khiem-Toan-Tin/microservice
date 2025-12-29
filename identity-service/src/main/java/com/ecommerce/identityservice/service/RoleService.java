package com.ecommerce.identityservice.service;


import com.ecommerce.identityservice.dto.request.RoleCreateForm;
import com.ecommerce.identityservice.dto.request.RoleEditForm;
import com.ecommerce.identityservice.dto.response.RoleDTO;
import com.ecommerce.identityservice.dto.response.RoleDetailDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface RoleService {
    Page<RoleDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    RoleDetailDTO findById(String id);
    void createRole(RoleCreateForm roleForm);
    void updateRole(RoleEditForm roleForm, String id);
    void deleteRole(String id);
}
