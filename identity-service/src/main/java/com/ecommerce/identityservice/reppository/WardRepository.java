package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.WardEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WardRepository extends JpaRepository<WardEntity, String> {
    List<WardEntity> findAllByDistrictCode(String districtCode);
}
