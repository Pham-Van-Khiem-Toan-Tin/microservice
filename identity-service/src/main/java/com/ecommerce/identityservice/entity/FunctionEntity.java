package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Set;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "functions",
        uniqueConstraints =
        @UniqueConstraint(name = "UniqueNameAndClient",
                columnNames = {"function_id", "client_id"}))
public class FunctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, name = "function_id")
    private String functionId;
    @Column(nullable = false, name = "function_name")
    private String functionName;
    @Column(nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
    @ManyToMany(mappedBy = "functions")
    private Set<RoleEntity> roles;
    @OneToMany(mappedBy = "function")
    private Set<SubFunctionEntity> subFunctions;
    @OneToMany(mappedBy = "function")
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
