package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "role_function_subfunction")
public class RoleFunctionSubFunctionEntity {
    @EmbeddedId
    private RoleFunctionSubFunctionId id;

    @ManyToOne
    @JoinColumn(name = "role_id", insertable = false, updatable = false)
    private RoleEntity role;

    @ManyToOne
    @JoinColumn(name = "function_id", insertable = false, updatable = false)
    private FunctionEntity function;

    @ManyToOne
    @JoinColumn(name = "subfunction_id", insertable = false, updatable = false)
    private SubFunctionEntity subFunction;
}
