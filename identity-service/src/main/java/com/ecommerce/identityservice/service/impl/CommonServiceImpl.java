package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.dto.CountryDTO;
import com.ecommerce.identityservice.dto.ProvinceDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.CountryEntity;
import com.ecommerce.identityservice.entity.ProvinceEntity;
import com.ecommerce.identityservice.mapper.CommonMapper;
import com.ecommerce.identityservice.repository.CountryRepository;
import com.ecommerce.identityservice.repository.ProvinceRepository;
import com.ecommerce.identityservice.service.CommonService;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CommonServiceImpl implements CommonService {
    @Autowired
    CountryRepository countryRepository;
    @Autowired
    ProvinceRepository provinceRepository;
    @Autowired
    CommonMapper commonMapper;
    @Autowired
    EntityManager entityManager;
    @Override
    public List<CountryDTO> allCountries() throws CustomException {
        List<CountryEntity> countryEntities = countryRepository.findAll();
        if (countryEntities == null)
            throw new CustomException(500, "Không thể tìm thấy quốc gia");
        return countryEntities.stream().map(item -> commonMapper.toCountryDTO(item)).collect(Collectors.toList());
    }

    @Override
    public List<ProvinceDTO> allProvinceOfCountry(String countryId) throws CustomException {
        List<ProvinceEntity> provinceEntities = provinceRepository.findAllByCountryId(countryId);
        if (provinceEntities == null)
            throw new CustomException(500, "Không thể tìm thấy quốc gia");
        return provinceEntities.stream().map(item -> commonMapper.toProvinceDTO(item)).collect(Collectors.toList());
    }
}
