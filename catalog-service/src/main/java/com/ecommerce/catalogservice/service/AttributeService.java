package com.ecommerce.catalogservice.service;


import com.ecommerce.catalogservice.dto.request.attribute.*;
import com.ecommerce.catalogservice.dto.response.attribute.AttributeDTO;
import com.ecommerce.catalogservice.dto.response.attribute.AttributeDetailDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

public interface AttributeService {
    Page<AttributeDTO> search(String keyword, List<AttributeSearchField> fields, Pageable pageable);
    List<AttributeDetailDTO> searchAttributeOption(@RequestBody AttributeOptionForm form);
    AttributeDetailDTO getAttributeDetail(String id);
    void addAttribute(AttributeCreateForm form);
    void updateAttribute(AttributeEditForm form, String id);
    void deleteAttribute(String id);
    void changeActiveAttribute(String id);
    void revokeAttribute(String id);
    void revokeAttributeOption(String id, RevokeOptionForm form);
}
