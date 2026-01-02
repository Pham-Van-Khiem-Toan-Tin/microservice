package com.ecommerce.catalogservice.entity;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecBlock {
    private String groupName;
    private Integer displayOrder;
    private List<SpecAttr> attributes;
}
