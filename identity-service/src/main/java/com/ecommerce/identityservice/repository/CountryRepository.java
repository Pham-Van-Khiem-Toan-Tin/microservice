package com.ecommerce.identityservice.repository;

import com.ecommerce.identityservice.entity.CountryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CountryRepository extends JpaRepository<CountryEntity, String> {

}
