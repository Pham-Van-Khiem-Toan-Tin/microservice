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
@NoArgsConstructor
@Table(name = "roles",
        uniqueConstraints =
        @UniqueConstraint(name = "UniqueNameAndClient",
                columnNames = {"role_id", "client_id"}))
@Builder
@AllArgsConstructor
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(nullable = false, name = "role_id")
    private String roleId;
    @Column(nullable = false, name = "role_name")
    private String roleName;
    @Column(nullable = false)
    private String description;
    @Column(nullable = false, name = "order_value")
    private int orderValue;
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<UserRole> users;
    @ManyToMany
    @JoinTable(
            name = "role_functions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "function_id"))
    private List<FunctionEntity> functions;
    @ManyToOne
    @JoinColumn(name = "client_id", nullable = false)
    private ClientEntity client;
    @OneToMany(mappedBy = "role", fetch = FetchType.EAGER)
    private List<RoleFunctionSubFunctionEntity> roleFunctionSubFunction;
}
