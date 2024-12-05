package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.CountryDTO;
import com.ecommerce.identityservice.dto.ProvinceDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;

import java.util.List;

public interface CommonService {
    List<CountryDTO> allCountries() throws CustomException;
    List<ProvinceDTO> allProvinceOfCountry(String countryId) throws CustomException;
}
