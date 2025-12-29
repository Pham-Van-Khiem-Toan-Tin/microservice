package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "subFunctions", indexes = {
        @Index(name = "idx_code", columnList = "code")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubFunctionEntity {
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
    @Column(nullable = false)
    private int sortOrder;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "function_id")
    private FunctionEntity function;
    @ManyToMany(mappedBy = "subFunctions")
    @Builder.Default
    private Set<RoleEntity> roles = new HashSet<>();
}
