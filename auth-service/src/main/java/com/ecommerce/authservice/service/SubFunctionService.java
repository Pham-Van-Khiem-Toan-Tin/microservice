package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.request.SubFunctionCreateForm;
import com.ecommerce.authservice.dto.request.SubFunctionEditForm;
import com.ecommerce.authservice.dto.request.SubFunctionForm;
import com.ecommerce.authservice.dto.request.SubFunctionOptionForm;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.entity.SubFunctionEntity;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface SubFunctionService {
    Set<SubFunctionDTO> getUnlinkedSubFunctions(SubFunctionOptionForm  subFunctionOptionForm);
    Page<SubFunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    SubFunctionEntity createSubFunction(SubFunctionCreateForm subFunctionForm);
    void updateSubFunction(SubFunctionEditForm subFunctionForm, String id);
    SubFunctionDTO getSubFunction(String id);
    void deleteSubFunction(String id);
}
