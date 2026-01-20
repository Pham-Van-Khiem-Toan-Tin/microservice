package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.request.AddressCreateForm;
import com.ecommerce.identityservice.dto.request.AddressUpdateForm;
import com.ecommerce.identityservice.dto.request.UserAddressDto;
import com.ecommerce.identityservice.dto.response.AddressDTO;
import com.ecommerce.identityservice.dto.response.AddressLocationDto;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    List<AddressLocationDto> getAllProvinces();
    List<AddressLocationDto> getDistricts(String provinceCode);
    List<AddressLocationDto> getWards(String districtCode);
    void createUserAddress(AddressCreateForm form);
    List<UserAddressDto> getMyAddresses();
    void updateUserAddress(String id, AddressUpdateForm form);
    void deleteUserAddress(String id);
    void setDefaultAddress(String id);

    AddressDTO getAddress(String id);
}
