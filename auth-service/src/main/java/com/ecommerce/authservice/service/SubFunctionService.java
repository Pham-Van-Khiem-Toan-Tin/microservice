package com.ecommerce.authservice.service;


import com.ecommerce.authservice.dto.response.SubFunctionDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface SubFunctionService {
    Set<SubFunctionDTO> getUnlinkedSubFunctions();
    Page<SubFunctionDTO> search(String keyword, List<String> fields, String sort, int page, int size);
}
