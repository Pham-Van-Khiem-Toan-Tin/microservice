package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.DistrictEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<DistrictEntity, String> {
    List<DistrictEntity> findAllByProvinceCode(String provinceCode);
}
