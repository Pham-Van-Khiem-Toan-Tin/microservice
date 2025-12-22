package com.ecommerce.authservice.service.impl;

import com.ecommerce.authservice.dto.response.AllFunctionDTO;
import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import com.ecommerce.authservice.entity.FunctionEntity;
import com.ecommerce.authservice.repository.FunctionRepository;
import com.ecommerce.authservice.service.FunctionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FunctionServiceImpl implements FunctionService {
    @Autowired
    private FunctionRepository functionRepository;
    @Override
    public Set<AllFunctionDTO> findAllFunction() {
        Set<FunctionEntity> functions = new HashSet<>(functionRepository.findAllWithSubFunctions());
        return functions.stream().map((f) -> AllFunctionDTO.builder()
                .id(f.getId())
                .name(f.getName())
                .description(f.getDescription())
                .sortOrder(f.getSortOrder())
                .subFunctions(f.getSubFunctions().stream().map(sf ->
                        SubFunctionDTO.builder()
                                .id(sf.getId())
                                .name(sf.getName())
                                .description(sf.getDescription())
                                .sortOrder(sf.getSortOrder())
                                .build()).collect(Collectors.toSet()))
                .build()).collect(Collectors.toSet());
    }
}
