package com.ecommerce.catalogservice.dto.request.attribute;

import lombok.Data;

@Data
public class RevokeOptionForm {
    private String attributeId;
    private String optionId;
}
