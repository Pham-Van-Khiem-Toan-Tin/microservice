package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subFunctions")
@Data
public class SubFunctionEntity {
    @Id
    private String id;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String description;
    @Column(nullable = false)
    private int sortOrder;
    @ManyToOne
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;
    @ManyToMany(mappedBy = "subFunctions")
    private Set<RoleEntity> roles = new HashSet<>();
}
