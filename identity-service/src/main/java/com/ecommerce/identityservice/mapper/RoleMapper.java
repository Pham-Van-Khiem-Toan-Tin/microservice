package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoleMapper {
    public RoleEntity toRoleEntity(AuthoritiesForm form) {
        if (form == null)
            return null;
        return RoleEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(form.getName())
                .description(form.getDescription())
                .normalizedName(form.getName().toUpperCase())
                .build();
    }
    public FunctionEntity toFunctionEntity(AuthoritiesForm form) {
        if (form == null)
            return null;
        return FunctionEntity.builder()
                .id(UUID.randomUUID().toString())
                .name(form.getName())
                .description(form.getDescription())
                .normalizedName(form.getName().toUpperCase())
                .build();
    }
}
