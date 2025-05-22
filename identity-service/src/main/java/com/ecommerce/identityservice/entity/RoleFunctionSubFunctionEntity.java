package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@NoArgsConstructor
@Table(name = "role_function_subfunction")
public class RoleFunctionSubFunctionEntity {
    @EmbeddedId
    private RoleFunctionSubFunctionId id = new RoleFunctionSubFunctionId();

    @ManyToOne(fetch = FetchType.EAGER)
    @MapsId("roleId")
    @JsonIgnore
    @JoinColumn(name = "role_id")
    private RoleEntity role;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @MapsId("functionId")
    @JoinColumn(name = "function_id")
    private FunctionEntity function;

    @ManyToOne(fetch = FetchType.EAGER)
    @JsonIgnore
    @MapsId("subFunctionId")
    @JoinColumn(name = "subfunction_id")
    private SubFunctionEntity subFunction;
}
