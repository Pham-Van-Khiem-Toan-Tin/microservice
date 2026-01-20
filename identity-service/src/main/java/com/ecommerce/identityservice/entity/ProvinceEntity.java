package com.ecommerce.identityservice.entity;


import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Entity
@Table(name = "provinces")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProvinceEntity {
    @Id
    @Column(name = "code", length = 20)
    private String code; // Dùng String vì code có thể là "01"

    @Column(name = "name")
    private String name;

    @Column(name = "code_name")
    private String codeName;

    @Column(name = "division_type")
    private String divisionType;

    @Column(name = "phone_code")
    private Integer phoneCode;

    // Quan hệ 1-N: Một tỉnh có nhiều huyện
    // mappedBy = "province" trỏ tới tên biến 'province' trong class District
    @OneToMany(mappedBy = "province", fetch = FetchType.LAZY)
    @JsonIgnore // Quan trọng: Khi lấy Tỉnh, không tự động load hết Huyện (để API nhẹ)
    private List<DistrictEntity> districts;
}
