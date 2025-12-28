package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.*;

@Entity
@Table(name = "roles", indexes = {
        @Index(name = "idx_code", columnList = "code")
})
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RoleEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @JdbcTypeCode(SqlTypes.BINARY)
    @Column(length = 16)
    private UUID id;
    @Column(nullable = false, unique = true)
    private String code;
    @Column(nullable = false, length = 100)
    private String name;
    @Column(nullable = false, length = 100)
    private String description;
    @Column
    private Integer sortOrder;
    @OneToMany(mappedBy = "role")
    private List<UserEntity> users = new ArrayList<>();
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "role_sub_functions",
            joinColumns = @JoinColumn(name = "role_id"),
            inverseJoinColumns = @JoinColumn(name = "sub_function_id")
    )
    @Builder.Default
    private Set<SubFunctionEntity> subFunctions = new HashSet<>();
}
