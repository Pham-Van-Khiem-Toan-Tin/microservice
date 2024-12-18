package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.ProfileDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public ProfileDTO toProfileDTO(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return ProfileDTO
                .builder()
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .avatar(userEntity.getAvatar())
                .role(userEntity.getRole().getRoleName())
                .build();
    }
    public ProfileDetailDTO toProfileDetailDTO(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return ProfileDetailDTO
                .builder()
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .avatar(userEntity.getAvatar())
                .phoneNumber(userEntity.getPhoneNumber())
                .build();
    }
}
