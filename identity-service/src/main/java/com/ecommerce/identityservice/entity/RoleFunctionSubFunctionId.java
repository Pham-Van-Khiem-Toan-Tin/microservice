package com.ecommerce.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Embeddable
public class RoleFunctionSubFunctionId implements Serializable {
    private String roleId;
    private String functionId;
    private String subFunctionId;

    public String getRoleId() {
        return roleId;
    }

    public void setRoleId(String roleId) {
        this.roleId = roleId;
    }

    public String getFunctionId() {
        return functionId;
    }

    public void setFunctionId(String functionId) {
        this.functionId = functionId;
    }

    public String getSubFunctionId() {
        return subFunctionId;
    }

    public void setSubFunctionId(String subFunctionId) {
        this.subFunctionId = subFunctionId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof RoleFunctionSubFunctionId)) return false;
        RoleFunctionSubFunctionId roleFunctionSubFunctionId = (RoleFunctionSubFunctionId) o;
        return Objects.equals(getRoleId(), roleFunctionSubFunctionId.getRoleId()) &&
                Objects.equals(getFunctionId(), roleFunctionSubFunctionId.getFunctionId()) &&
                Objects.equals(getSubFunctionId(), roleFunctionSubFunctionId.getSubFunctionId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getRoleId(), getFunctionId(), getSubFunctionId());
    }
}
