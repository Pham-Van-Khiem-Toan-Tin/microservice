package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@NoArgsConstructor
@Table(name = "subfunctions",
        uniqueConstraints =
        @UniqueConstraint(name = "UniqueNameAndClient",
                columnNames = {"name", "client_id"}))
public class SubFunctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, name = "normalized_name")
    private String normalizedName;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String description;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
    @ManyToOne
    @JoinColumn(name = "function_id", nullable = false)
    private FunctionEntity function;
    @OneToMany(mappedBy = "subFunction")
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
