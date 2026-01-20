package com.ecommerce.identityservice.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "wards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WardEntity {
    @Id
    @Column(name = "code", length = 20)
    private String code;

    @Column(name = "name")
    private String name;

    @Column(name = "code_name")
    private String codeName;

    @Column(name = "division_type")
    private String divisionType;

    @Column(name = "short_codename")
    private String shortCodename;

    // Quan hệ N-1: Nhiều xã thuộc 1 huyện
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_code")
    @JsonIgnore
    private DistrictEntity district;
}
