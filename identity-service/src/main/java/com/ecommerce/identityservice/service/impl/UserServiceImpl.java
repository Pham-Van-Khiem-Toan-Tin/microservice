package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.dto.request.AddressForm;
import com.ecommerce.identityservice.dto.request.AddressUpdateForm;
import com.ecommerce.identityservice.dto.request.UpdateProfileForm;
import com.ecommerce.identityservice.dto.request.UpdateProfileRequest;
import com.ecommerce.identityservice.dto.response.BusinessException;
import com.ecommerce.identityservice.dto.response.UserProfileDto;
import com.ecommerce.identityservice.dto.response.UserResponse;
import com.ecommerce.identityservice.dto.response.user.UserProfileResponse;
import com.ecommerce.identityservice.dto.response.user.UserSummaryResponse;
import com.ecommerce.identityservice.entity.AddressType;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.UserAddressEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.integration.PaymentFeignClient;
import com.ecommerce.identityservice.reppository.RoleRepository;
import com.ecommerce.identityservice.reppository.UserAddressRepository;
import com.ecommerce.identityservice.reppository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import com.ecommerce.identityservice.utils.AuthenticationUtils;
import jakarta.persistence.criteria.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.ecommerce.identityservice.constants.Constants.*;

@Service
@Slf4j
public  class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserAddressRepository addressRepository;
    @Autowired
    private PaymentFeignClient paymentFeignClient;
    @Autowired
    private RoleRepository roleRepository;
    @Override
    @Transactional
    public List<RoleEntity> getAllRoles() {
        return roleRepository.findAll();
    }
    public List<UserAddressEntity> getAllAddresses() {
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        return addressRepository.findByUserId(userId);
    }
    @Transactional
    @Override
    public void updateUserRole(String userId, String roleId) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(USER_NOTFOUND));
        RoleEntity role = roleRepository.findById(UUID.fromString(roleId))
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        user.setRole(role);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public void updateUserStatus(String userId, int status, UserEntity admin) {
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new BusinessException(USER_NOTFOUND));
        user.setStatus(status);
        user.setUpdatedBy(admin);
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    @Override
    public Page<UserSummaryResponse> getAllUsers(String keyword, Integer status, Pageable pageable) {
        Specification<UserEntity> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (keyword != null && !keyword.isBlank()) {
                String lk = "%" + keyword.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("email")), lk),
                        cb.like(cb.lower(root.get("firstName")), lk),
                        cb.like(cb.lower(root.get("lastName")), lk),
                        cb.like(root.get("phone"), lk)
                ));
            }

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return userRepository.findAll(spec, pageable).map(this::mapToSummary);
    }
    private UserSummaryResponse mapToSummary(UserEntity entity) {
        return UserSummaryResponse.builder()
                .id(entity.getId().toString())
                .fullName(entity.getFullName())
                .email(entity.getEmail())
                .phone(entity.getPhone())
                .avatarUrl(entity.getAvatarUrl())
                .roleName(entity.getRole() != null ? entity.getRole().getName() : "N/A")
                .status(entity.getStatus())
                .createdAt(entity.getCreatedAt())
                .build();
    }
    @Override
    public UserProfileDto getMyProfile() {
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new BusinessException(USER_NOTFOUND)
                );
        BigDecimal balance = BigDecimal.ZERO;
        try {
            balance = paymentFeignClient.getWalletBalance();
        } catch (Exception e) {
            log.error("Không thể lấy số dư ví cho user {}: {}", userId, e.getMessage());
        }
        return UserProfileDto.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .avatarPublicId(user.getPublicAvatarId())
                 .joinDate(user.getCreatedAt().atZone(ZoneId.of("Asia/Ho_Chi_Minh")).toLocalDate())
                .walletBalance(balance)
                .build();
    }

    @Override
    public void updateProfile(UpdateProfileForm updateProfileForm) {
        if (!StringUtils.hasText(updateProfileForm.getFirstName())
        || !StringUtils.hasText(updateProfileForm.getLastName())
        || !StringUtils.hasText(updateProfileForm.getPhone())
        || !updateProfileForm.getPhone().matches("^0\\d{9}$")) {
            throw new BusinessException(VALIDATE_FAIL);
        }
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new BusinessException(USER_NOTFOUND)
                );
        user.setFirstName(updateProfileForm.getFirstName());
        user.setLastName(updateProfileForm.getLastName());
        user.setPhone(updateProfileForm.getPhone());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);

    }
    @Transactional
    @Override
    public void addAddress(AddressForm form) {
        validateInput(form);
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new BusinessException(USER_NOTFOUND)
                );
        long count = addressRepository.countByUserId(userId);
        boolean isDefault = (count == 0) || Boolean.TRUE.equals(form.getIsDefault());
        if (isDefault) {
            unsetOldDefault(userId);
        }
        UserAddressEntity address = UserAddressEntity.builder()
                .user(user) // Link với UserEntity
                .receiverName(form.getReceiverName())
                .phone(form.getPhone())
                .detailAddress(form.getDetailAddress())
                .provinceCode(form.getProvinceCode())
                .provinceName(form.getProvinceName())
                .districtCode(form.getDistrictCode())
                .districtName(form.getDistrictName())
                .wardCode(form.getWardCode())
                .wardName(form.getWardName())
                .isDefault(isDefault)
                .type(AddressType.HOME) // Mặc định set là HOME
                .build();

        addressRepository.save(address);
    }
    @Transactional
    @Override
    public void updateAddress(String addressId, AddressUpdateForm form) {
        validateInputUpdate(form, addressId);
        UUID userId = UUID.fromString(AuthenticationUtils.getUserId());
        UUID addrUuid = UUID.fromString(addressId);
        UserAddressEntity address = addressRepository.findById(addrUuid)
                .orElseThrow(() -> new BusinessException(VALIDATE_FAIL));
        if (!address.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized update");
        }

        // Logic Default
        if (Boolean.TRUE.equals(form.getIsDefault())) {
            unsetOldDefault(userId);
            address.setIsDefault(true);
        }
        address.setReceiverName(form.getReceiverName());
        address.setPhone(form.getPhone());
        address.setDetailAddress(form.getDetailAddress());

        address.setProvinceCode(form.getProvinceCode());
        address.setProvinceName(form.getProvinceName());
        address.setDistrictCode(form.getDistrictCode());
        address.setDistrictName(form.getDistrictName());
        address.setWardCode(form.getWardCode());
        address.setWardName(form.getWardName());

        addressRepository.save(address);
    }

    @Override
    public void deleteAddress(String addressId) {

    }

    @Override
    public UserResponse getUser() {
        UserEntity user = userRepository.findById(UUID.fromString(AuthenticationUtils.getUserId()))
                .orElseThrow(() -> new BusinessException(USER_NOTFOUND));
        return UserResponse.builder()
                .id(user.getId().toString())
                .avatarUrl(user.getAvatarUrl())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .build();
    }

    @Override
    public UserProfileResponse getUserProfile() {
        String userId = AuthenticationUtils.getUserId();
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        return UserProfileResponse.builder()
                .id(user.getId().toString())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .verifyEmail(user.getVerifyEmail())
                .createdAt(user.getCreatedAt())
                .build();
    }
    @Transactional
    @Override
    public void updateUserProfile(UpdateProfileRequest request) {
        String userId = AuthenticationUtils.getUserId();
        UserEntity user = userRepository.findById(UUID.fromString(userId))
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhone(request.getPhone());
        user.setUpdatedAt(Instant.now());
        userRepository.save(user);
    }

    private void unsetOldDefault(UUID userId) {
        addressRepository.findFirstByUserIdAndIsDefaultTrue(userId)
                .ifPresent(addr -> {
                    addr.setIsDefault(false);
                    addressRepository.save(addr);
                });
    }
    private void validateInputUpdate(AddressUpdateForm req, String addressId) {
        if (!StringUtils.hasText(req.getId()) || !req.getId().equals(addressId))
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getReceiverName() == null || req.getReceiverName().trim().isEmpty())
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getPhone() == null || !req.getPhone().matches("^0\\d{9}$"))
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getDetailAddress() == null || req.getDetailAddress().trim().isEmpty())
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getProvinceCode() == null) throw new BusinessException(VALIDATE_FAIL);
        if (req.getDistrictCode() == null) throw new BusinessException(VALIDATE_FAIL);
        if (req.getWardCode() == null) throw new BusinessException(VALIDATE_FAIL);
    }
    private void validateInput(AddressForm req) {
        if (req.getReceiverName() == null || req.getReceiverName().trim().isEmpty())
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getPhone() == null || !req.getPhone().matches("^0\\d{9}$"))
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getDetailAddress() == null || req.getDetailAddress().trim().isEmpty())
            throw new BusinessException(VALIDATE_FAIL);
        if (req.getProvinceCode() == null) throw new BusinessException(VALIDATE_FAIL);
        if (req.getDistrictCode() == null) throw new BusinessException(VALIDATE_FAIL);
        if (req.getWardCode() == null) throw new BusinessException(VALIDATE_FAIL);
    }
}
