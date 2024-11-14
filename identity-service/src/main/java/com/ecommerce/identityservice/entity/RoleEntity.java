package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@NoArgsConstructor
@Table(name = "roles")
public class RoleEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false, name = "order_value")
    private int orderValue;
    @OneToMany(mappedBy = "role")
    private Set<UserEntity> users;
    @ManyToMany
    @JoinTable(
            name = "role_functions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "function_id"))
    private Set<FunctionEntity> functions;
    @OneToMany(mappedBy = "role")
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
