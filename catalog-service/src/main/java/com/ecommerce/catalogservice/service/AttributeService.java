package com.ecommerce.catalogservice.service;


import com.ecommerce.catalogservice.dto.request.*;
import com.ecommerce.catalogservice.dto.response.AttributeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

public interface AttributeService {
    Page<AttributeDTO> search(String keyword, List<AttributeSearchField> fields, Pageable pageable);
    AttributeDetailDTO getAttributeDetail(String id);
    void addAttribute(AttributeCreateForm form);
    void updateAttribute(AttributeEditForm form, String id);
    void deleteAttribute(String id);

}
