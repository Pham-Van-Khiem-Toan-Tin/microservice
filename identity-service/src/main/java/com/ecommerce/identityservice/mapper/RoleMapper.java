package com.ecommerce.identityservice.mapper;

import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import com.ecommerce.identityservice.form.AuthoritiesWithParentForm;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class RoleMapper {
    public RoleEntity toRoleEntity(AuthoritiesForm form) {
        if (form == null)
            return null;
        return RoleEntity.builder()
                .id(UUID.randomUUID().toString())
                .roleName(form.getName())
                .description(form.getDescription())
                .roleId(form.getId())
                .build();
    }
    public FunctionEntity toFunctionEntity(AuthoritiesForm form) {
        if (form == null)
            return null;
        return FunctionEntity.builder()
                .id(UUID.randomUUID().toString())
                .functionName(form.getName())
                .description(form.getDescription())
                .functionId(form.getId())
                .build();
    }
    public SubFunctionEntity toSubFunctionEntity(AuthoritiesWithParentForm form) {
        if (form == null)
            return null;
        return SubFunctionEntity.builder()
                .id(UUID.randomUUID().toString())
                .subfunctionId(form.getId())
                .subFunctionName(form.getName())
                .description(form.getDescription())
                .build();
    }
}
