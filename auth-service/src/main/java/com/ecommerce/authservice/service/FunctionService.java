package com.ecommerce.authservice.service;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;

import java.util.Set;

public interface FunctionService {
    Set<AllFunctionDTO> findAllFunction();
}
