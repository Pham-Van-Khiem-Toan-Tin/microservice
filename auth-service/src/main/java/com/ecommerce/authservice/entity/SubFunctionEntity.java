package com.ecommerce.authservice.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "subFunctions")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubFunctionEntity {
    @Id
    private String id;
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
