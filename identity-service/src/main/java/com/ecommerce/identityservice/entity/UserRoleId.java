package com.ecommerce.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

@Data
@Embeddable
public class UserRoleId implements Serializable {
    private String userId;

    private String clientId;

    public String getUserId() {
        return userId;
    }

    public String getClientId() {
        return clientId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof UserRoleId)) return false;
        UserRoleId userRoleId = (UserRoleId) o;
        return Objects.equals(getUserId(), userRoleId.getUserId()) &&
                Objects.equals(getClientId(), userRoleId.getClientId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getUserId(), getClientId());
    }
}
