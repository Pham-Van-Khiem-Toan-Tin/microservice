package com.ecommerce.catalogservice.service;


import com.ecommerce.catalogservice.dto.request.*;
import com.ecommerce.catalogservice.dto.response.AttributeDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

public interface AttributeService {
    Page<AttributeDTO> search(String keyword, List<AttributeSearchField> fields, Pageable pageable);
    List<AttributeDetailDTO> searchAttributeOption(@RequestBody AttributeOptionForm form);
    AttributeDetailDTO getAttributeDetail(String id);
    void addAttribute(AttributeCreateForm form);
    void updateAttribute(AttributeEditForm form, String id);
    void deleteAttribute(String id);

}
