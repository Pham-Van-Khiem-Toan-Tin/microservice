package com.ecommerce.identityservice.reppository;

import com.ecommerce.identityservice.entity.ProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProvinceRepository extends JpaRepository<ProvinceEntity, String> {
}
