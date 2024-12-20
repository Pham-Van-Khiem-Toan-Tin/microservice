package com.ecommerce.identityservice.service;

import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import com.ecommerce.identityservice.form.AuthoritiesWithParentForm;

public interface AuthoritiesService {
    RoleEntity createRole(AuthoritiesForm form) throws CustomException;
    FunctionEntity createFunction(AuthoritiesForm form) throws CustomException;
    SubFunctionEntity createSubFunction(AuthoritiesWithParentForm form) throws CustomException;
}
