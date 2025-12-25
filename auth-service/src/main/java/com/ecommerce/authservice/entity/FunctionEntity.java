package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "functions")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionEntity {
    @Id
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String description;
    @Column(nullable = false)
    private Integer sortOrder;
    @Column(nullable = false)
    private String icon;
    @OneToMany(mappedBy = "function", fetch = FetchType.LAZY)
    private Set<SubFunctionEntity> subFunctions = new HashSet<>();
}
