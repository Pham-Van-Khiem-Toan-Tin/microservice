package com.ecommerce.catalogservice.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecGroupTemplate {
    private String name;
    private String displayOrder;
    private List<TemplateField> fields;
}
