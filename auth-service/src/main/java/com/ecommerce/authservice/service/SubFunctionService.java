package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.request.SubFunctionForm;
import com.ecommerce.authservice.dto.request.SubFunctionOptionForm;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface SubFunctionService {
    Set<SubFunctionDTO> getUnlinkedSubFunctions(SubFunctionOptionForm  subFunctionOptionForm);
    Page<SubFunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    void createSubFunction(SubFunctionForm subFunctionForm);
    void updateSubFunction(SubFunctionForm subFunctionForm, String id);
    SubFunctionDTO getSubFunction(String id);
    void deleteSubFunction(String id);
}
