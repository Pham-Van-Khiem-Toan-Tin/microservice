package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public ProfileDetailDTO toProfileDetailDTO(UserEntity userEntity) {
        if (userEntity == null)
            return null;
        return ProfileDetailDTO
                .builder()
                .email(userEntity.getEmail())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .role(userEntity.getRole().getName())
                .build();
    }
}
