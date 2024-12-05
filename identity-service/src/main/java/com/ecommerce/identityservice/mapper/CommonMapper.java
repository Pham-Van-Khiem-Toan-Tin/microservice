package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.CountryDTO;
import com.ecommerce.identityservice.dto.ProvinceDTO;
import com.ecommerce.identityservice.entity.CountryEntity;
import com.ecommerce.identityservice.entity.ProvinceEntity;
import org.springframework.stereotype.Component;

@Component
public class CommonMapper {
    public CountryDTO toCountryDTO(CountryEntity countryEntity) {
        return CountryDTO.builder()
                .id(countryEntity.getId())
                .name(countryEntity.getName())
                .build();
    }
    public ProvinceDTO toProvinceDTO(ProvinceEntity provinceEntity) {
        return ProvinceDTO.builder()
                .id(provinceEntity.getId())
                .name(provinceEntity.getName())
                .build();
    }
}
