package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "subfunctions")
public class SubFunctionEntity {
    @Id
    private String id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;
    @OneToMany(mappedBy = "subFunction")
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
