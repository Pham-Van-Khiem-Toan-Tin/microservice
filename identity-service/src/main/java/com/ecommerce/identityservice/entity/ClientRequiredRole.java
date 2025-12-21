package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "client_required_roles")
@Builder
@AllArgsConstructor
public class ClientRequiredRole {
    @EmbeddedId
    private ClientRequiredRoleId id;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", referencedColumnName = "id",
            insertable = false, updatable = false)
    private RoleEntity role;

    public ClientRequiredRole() {}
    public ClientRequiredRole(String clientId, String roleCode) {
        this.id = new ClientRequiredRoleId(clientId, roleCode);
    }
}
