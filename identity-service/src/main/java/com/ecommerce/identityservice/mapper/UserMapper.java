package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring")
public interface UserMapper {
    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);
    ProfileDetailDTO toProfileDetailDTO(UserEntity userEntity);
}
