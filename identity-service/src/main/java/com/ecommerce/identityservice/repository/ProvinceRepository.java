package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.ProvinceEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProvinceRepository extends JpaRepository<ProvinceEntity, Long> {
    @Query(value = "select * from provinces where country = :countryId", nativeQuery = true)
    List<ProvinceEntity> findAllByCountryId(@Param("countryId") String countryId);
}
