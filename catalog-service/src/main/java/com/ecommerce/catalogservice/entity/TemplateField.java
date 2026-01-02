package com.ecommerce.catalogservice.entity;

import lombok.*;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TemplateField {
    private String k;
    private boolean required;
    private boolean facet;
    private String dataType;
    private List<String> allowedOptions;
    private String displayName;
    private  Integer displayOrder;
    private Map<String, Object> validation;

    private boolean disabled;
}
