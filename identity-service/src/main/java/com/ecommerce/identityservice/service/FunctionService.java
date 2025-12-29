package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.FunctionEditForm;
import com.ecommerce.identityservice.dto.request.FunctionForm;
import com.ecommerce.identityservice.dto.response.AllFunctionDTO;
import com.ecommerce.identityservice.dto.response.FunctionDTO;
import com.ecommerce.identityservice.dto.response.FunctionDetailDTO;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.Set;

public interface FunctionService {
    Set<AllFunctionDTO> findAllFunction();
    FunctionDetailDTO findFunctionById(String id);
    Set<FunctionDTO> findAllFunctionsOptions();
    Page<FunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size);
    void createFunction(FunctionForm functionForm);
    void editFunction(FunctionEditForm functionEditFormForm, String id);
    void deleteFunction(String id);
}
