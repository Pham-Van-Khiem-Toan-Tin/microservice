package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.dto.response.AddressLocationDto;
import com.ecommerce.identityservice.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/locations")
public class LocationController {
    @Autowired
    private AddressService addressService;
    @GetMapping("/provinces")
    public List<AddressLocationDto> getProvinces() {
        return addressService.getAllProvinces();
    }

    @GetMapping("/districts/{provinceCode}")
    public List<AddressLocationDto> getDistricts(@PathVariable String provinceCode) {
        return addressService.getDistricts(provinceCode);
    }

    @GetMapping("/wards/{districtCode}")
    public List<AddressLocationDto> getWards(@PathVariable String districtCode) {
        return addressService.getWards(districtCode);
    }
}
