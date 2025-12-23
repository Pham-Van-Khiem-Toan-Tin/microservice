package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.dto.response.FunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface FunctionService {
    Set<AllFunctionDTO> findAllFunction();
    Page<FunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size);
}
