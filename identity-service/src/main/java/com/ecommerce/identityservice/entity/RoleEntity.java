package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "roles")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoleEntity implements Serializable {
//    private static final long serialVersionUID = 1L;
    @Id
    private String id;
    @Column(nullable = false, unique = true)
    private String name;
    @Column(nullable = false)
    private int position;
    @Column(nullable = false)
    private String description;
}
