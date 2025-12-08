package com.ecommerce.identityservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity {
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private int position;
    @Column(nullable = false)
    private String description;

    @OneToMany(mappedBy = "role")
    private List<UserEntity> users = new ArrayList<>();
}
