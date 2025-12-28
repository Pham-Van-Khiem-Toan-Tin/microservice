package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.request.FunctionEditForm;
import com.ecommerce.authservice.dto.request.FunctionForm;
import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.dto.response.FunctionDetailDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
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
