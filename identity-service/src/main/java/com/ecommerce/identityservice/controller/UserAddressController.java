package com.ecommerce.identityservice.controller;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.request.AddressCreateForm;
import com.ecommerce.identityservice.dto.request.AddressUpdateForm;
import com.ecommerce.identityservice.dto.request.UserAddressDto;
import com.ecommerce.identityservice.dto.response.AddressDTO;
import com.ecommerce.identityservice.dto.response.ApiResponse;
import com.ecommerce.identityservice.service.AddressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.ecommerce.identityservice.constants.Constants.*;

@RestController
@RequestMapping("/user-addresses")
public class UserAddressController {
    @Autowired
    private AddressService addressService;

    @PostMapping
    public ApiResponse<Void> create(@RequestBody AddressCreateForm form) {
        // Lấy userId từ SecurityContext (đã decode từ JWT)
        addressService.createUserAddress(form);
        return ApiResponse.ok(CREATE_ADDRESS_SUCCESS);
    }

    @GetMapping("/my")
    public List<UserAddressDto> getMyAddresses() {
        return addressService.getMyAddresses();
    }
    @PutMapping("/{id}")
    public ApiResponse<Void> update(@PathVariable String id, @RequestBody AddressUpdateForm form) {
        addressService.updateUserAddress(id, form);
        return ApiResponse.ok(UPDATE_ADDRESS_SUCCESS);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable String id) {
        addressService.deleteUserAddress(id);
        return ApiResponse.ok(DELETE_LOCATION_SUCCESS);
    }

    @PatchMapping("/{id}/set-default")
    public ApiResponse<Void> setDefault(@PathVariable String id) {
        addressService.setDefaultAddress(id);
        return ApiResponse.ok(UPDATE_ADDRESS_SUCCESS);
    }
    @GetMapping("/addresses/{id}")
    public AddressDTO getAddress(@PathVariable String id) {
        return addressService.getAddress(id);
    }
}
