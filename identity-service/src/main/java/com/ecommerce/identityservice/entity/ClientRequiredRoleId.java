package com.ecommerce.identityservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Embeddable
public class ClientRequiredRoleId implements Serializable {
    @Column(name = "client_id", length = 120)
    private String clientId;
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(name = "role_id", length = 16)
    private UUID roleId;

    public ClientRequiredRoleId() {}
    public ClientRequiredRoleId(String clientId, String roleCode) {
        this.clientId = clientId;
        this.roleId = roleId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ClientRequiredRoleId that)) return false;
        return Objects.equals(clientId, that.clientId)
                && Objects.equals(roleId, that.roleId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(clientId, roleId);
    }
}
