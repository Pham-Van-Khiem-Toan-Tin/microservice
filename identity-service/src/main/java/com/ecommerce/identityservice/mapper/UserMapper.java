package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.dto.ProfileDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Map;

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
//                .role(userEntity.getRoles().get)
                .build();
    }
    public static AuthProfileDTO toAuthProfileDTO(UserEntity user) {
        return AuthProfileDTO.builder()
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
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
