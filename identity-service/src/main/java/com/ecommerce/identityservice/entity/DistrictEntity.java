package com.ecommerce.identityservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "districts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DistrictEntity {
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

    // Quan hệ N-1: Nhiều huyện thuộc 1 tỉnh
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "province_code") // Tên cột khóa ngoại trong DB
    @JsonIgnore // Tránh vòng lặp vô tận khi serialize JSON
    private ProvinceEntity province;

    // Quan hệ 1-N: Một huyện có nhiều xã
    @OneToMany(mappedBy = "district", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<WardEntity> wards;
}
