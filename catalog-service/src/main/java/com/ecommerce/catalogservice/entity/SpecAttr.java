package com.ecommerce.catalogservice.entity;


import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecAttr {
    private String k;        // attributeCode

    private String vText;
    private Double vNum;
    private Boolean vBool;
    private List<String> vTexts;

    private String unit;
    private String displayName;
}
