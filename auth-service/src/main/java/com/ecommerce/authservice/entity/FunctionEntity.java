package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "functions")
@Data
public class FunctionEntity {
    @Id
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String description;
    @Column(nullable = false)
    private int sortOrder;
    @OneToMany(mappedBy = "function", fetch = FetchType.LAZY)
    private Set<SubFunctionEntity> subFunctions = new HashSet<>();
}
