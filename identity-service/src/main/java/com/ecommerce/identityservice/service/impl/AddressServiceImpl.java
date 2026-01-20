package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.constants.Constants;
import com.ecommerce.identityservice.dto.request.AddressCreateForm;
import com.ecommerce.identityservice.dto.request.AddressUpdateForm;
import com.ecommerce.identityservice.dto.request.UserAddressDto;
import com.ecommerce.identityservice.dto.response.AddressDTO;
import com.ecommerce.identityservice.dto.response.AddressLocationDto;
import com.ecommerce.identityservice.dto.response.BusinessException;
import com.ecommerce.identityservice.entity.AddressType;
import com.ecommerce.identityservice.entity.UserAddressEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.reppository.*;
import com.ecommerce.identityservice.service.AddressService;
import com.ecommerce.identityservice.utils.AuthenticationUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static com.ecommerce.identityservice.constants.Constants.*;

@Service
public class AddressServiceImpl implements AddressService {
    @Autowired
    private ProvinceRepository provinceRepo;
    @Autowired
    private DistrictRepository districtRepo;
    @Autowired
    private WardRepository wardRepo;
    @Autowired
    private UserAddressRepository userAddressRepo;
    @Autowired
    private UserRepository userRepo;

    @Override
    public List<AddressLocationDto> getAllProvinces() {
        return provinceRepo.findAll().stream()
                .map(p -> AddressLocationDto.builder().code(p.getCode()).name(p.getName()).build())
                .toList();
    }

    @Override
    public List<AddressLocationDto> getDistricts(String provinceCode) {
        return districtRepo.findAllByProvinceCode(provinceCode).stream()
                .map(d -> AddressLocationDto.builder().code(d.getCode()).name(d.getName()).build())
                .toList();
    }

    @Override
    public List<AddressLocationDto> getWards(String districtCode) {
        return wardRepo.findAllByDistrictCode(districtCode).stream()
                .map(w -> AddressLocationDto.builder().code(w.getCode()).name(w.getName()).build())
                .toList();
    }

    @Transactional
    @Override
    public void createUserAddress(AddressCreateForm form) {
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        if (Boolean.TRUE.equals(form.getIsDefault())) {
            userAddressRepo.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> addr.setIsDefault(false));
        }
        var province = provinceRepo.findById(form.getProvinceCode()).orElseThrow();
        var district = districtRepo.findById(form.getDistrictCode()).orElseThrow();
        var ward = wardRepo.findById(form.getWardCode()).orElseThrow();
        UserEntity user = userRepo.findById(userId)
                .orElseThrow(() -> new BusinessException(Constants.VALIDATE_FAIL));
        UserAddressEntity address = UserAddressEntity.builder()
                .user(user) // Giả sử đã có UserEntity
                .receiverName(form.getReceiverName())
                .phone(form.getPhone())
                .provinceCode(form.getProvinceCode())
                .provinceName(province.getName())
                .districtCode(form.getDistrictCode())
                .districtName(district.getName())
                .wardCode(form.getWardCode())
                .wardName(ward.getName())
                .detailAddress(form.getDetailAddress())
                .isDefault(form.getIsDefault())
                .type(form.getType())
                .build();
        userAddressRepo.save(address);
    }

    @Override
    public List<UserAddressDto> getMyAddresses() {
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        return userAddressRepo.findAllByUserIdOrderByIsDefaultDescCreatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }
    @Transactional
    @Override
    public void updateUserAddress(String id, AddressUpdateForm form) {
        UserAddressEntity address = userAddressRepo.findById(UUID.fromString(id))
                .orElseThrow(() -> new BusinessException(LOCATION_INVALID));

        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());

        // Xử lý nếu đặt địa chỉ này làm mặc định
        if (Boolean.TRUE.equals(form.getIsDefault())) {
            userAddressRepo.findByUserIdAndIsDefaultTrue(userId)
                    .ifPresent(addr -> addr.setIsDefault(false));
        }

        // Cập nhật thông tin cơ bản
        address.setReceiverName(form.getReceiverName());
        address.setPhone(form.getPhone());
        address.setDetailAddress(form.getDetailAddress());
        address.setIsDefault(form.getIsDefault());
        address.setType(form.getType());

        // Cập nhật thông tin vị trí (Nếu code thay đổi)
        if (!address.getProvinceCode().equals(form.getProvinceCode())) {
            var p = provinceRepo.findById(form.getProvinceCode()).orElseThrow();
            address.setProvinceCode(p.getCode());
            address.setProvinceName(p.getName());
        }
        // ... Tương tự cho District và Ward ...
        var d = districtRepo.findById(form.getDistrictCode()).orElseThrow();
        address.setDistrictCode(d.getCode());
        address.setDistrictName(d.getName());

        var w = wardRepo.findById(form.getWardCode()).orElseThrow();
        address.setWardCode(w.getCode());
        address.setWardName(w.getName());

        userAddressRepo.save(address);
    }
    @Transactional
    @Override
    public void deleteUserAddress(String id) {
        UserAddressEntity address = userAddressRepo.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(LOCATION_INVALID)
        );
        userAddressRepo.delete(address);
    }
    @Transactional
    @Override
    public void setDefaultAddress(String id) {
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());

        // Bỏ mặc định cũ
        userAddressRepo.findByUserIdAndIsDefaultTrue(userId)
                .ifPresent(addr -> addr.setIsDefault(false));

        // Thiết lập mặc định mới
        UserAddressEntity address = userAddressRepo.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(LOCATION_INVALID)
        );
        address.setIsDefault(true);
        userAddressRepo.save(address);
    }

    @Override
    public AddressDTO getAddress(String id) {
        UserAddressEntity address = userAddressRepo.findById(UUID.fromString(id)).orElseThrow(
                () -> new BusinessException(LOCATION_INVALID)
        );
        return AddressDTO.builder()
                .receiverName(address.getReceiverName())
                .phone(address.getPhone())
                .detailAddress(address.getDetailAddress())
                .districtName(address.getDistrictName())
                .wardName(address.getWardName())
                .provinceName(address.getProvinceName())
                .build();
    }

    private UserAddressDto mapToResponse(UserAddressEntity entity) {
        return UserAddressDto.builder()
                .id(entity.getId().toString())
                .receiverName(entity.getReceiverName())
                .phone(entity.getPhone())
                .provinceCode(entity.getProvinceCode())
                .provinceName(entity.getProvinceName())
                .districtCode(entity.getDistrictCode())
                .districtName(entity.getDistrictName())
                .wardCode(entity.getWardCode())
                .wardName(entity.getWardName())
                .detailAddress(entity.getDetailAddress())
                .isDefault(entity.getIsDefault())
                .type(entity.getType())
                .build();
    }
}
