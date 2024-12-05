package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.CountryDTO;
import com.ecommerce.identityservice.dto.ProvinceDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.service.CommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class CommonController {
    @Autowired
    private CommonService commonService;
    @GetMapping("/country/all")
    @PreAuthorize("hasAuthority('EDIT_PROFILE')")
    public ApiResponse<List<CountryDTO>> allCountries() throws CustomException {
        return new ApiResponse<>(200, commonService.allCountries());
    }
    @GetMapping("/country/{id}")
    @PreAuthorize("hasAuthority('EDIT_PROFILE')")
    public ApiResponse<List<ProvinceDTO>> allProvinceOfCountry(@PathVariable("id") String country) throws CustomException {
        return new ApiResponse<>(200, commonService.allProvinceOfCountry(country));
    }
}
