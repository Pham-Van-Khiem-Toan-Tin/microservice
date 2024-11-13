package com.ecommerce.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;

@Embeddable
@Data
public class RoleFunctionSubFunctionId implements Serializable {
    @Column(name = "role_id")
    private String roleId;

    @Column(name = "function_id")
    private String functionId;

    @Column(name = "subfunction_id")
    private String subFunctionId;
}
