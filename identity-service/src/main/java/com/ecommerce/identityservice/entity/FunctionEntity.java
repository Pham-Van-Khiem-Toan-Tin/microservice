package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "functions")
public class FunctionEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @ManyToMany(mappedBy = "functions")
    private Set<RoleEntity> roles;
    @OneToMany(mappedBy = "function")
    private Set<SubFunctionEntity> subFunctions;
    @OneToMany(mappedBy = "function")
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
