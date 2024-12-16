package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "subfunctions",
        uniqueConstraints =
        @UniqueConstraint(name = "UniqueNameAndClient",
                columnNames = {"subfunction_id", "client_id"}))
public class SubFunctionEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, name = "subfunction_id")
    private String subfunctionId;
    @Column(nullable = false, name = "subfucntion_name")
    private String subFunctionName;
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
