package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.ClientEntity;
import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import com.ecommerce.identityservice.mapper.RoleMapper;
import com.ecommerce.identityservice.repository.ClientRepository;
import com.ecommerce.identityservice.repository.FucntionRepository;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.service.AuthoritiesService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import static com.ecommerce.identityservice.constants.Constants.*;
@Service
public class AuthoritiesServiceImpl implements AuthoritiesService {
    @Autowired
    RoleRepository roleRepository;
    @Autowired
    FucntionRepository fucntionRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    RoleMapper roleMapper;
    @Override
    public RoleEntity createRole(AuthoritiesForm form) throws CustomException {
        if (validateAuthoritiesForm(form))
            throw new CustomException(ROLE_VALIDATE);
        ClientEntity client = clientRepository.findById(form.getClientId()).orElse(null);
        if (client == null)
            throw new CustomException(ROLE_VALIDATE);
        Boolean existed = roleRepository.existsByNameAndClientId(form.getName(), form.getClientId());
        if (existed)
            throw new CustomException(ROLE_EXISTS);
        RoleEntity newRole = roleMapper.toRoleEntity(form);
        newRole.setClient(client);
        return roleRepository.save(newRole);
    }

    @Override
    public FunctionEntity createFunction(AuthoritiesForm form) throws CustomException {
        if (validateAuthoritiesForm(form))
            throw new CustomException(ROLE_VALIDATE);
        ClientEntity client = clientRepository.findById(form.getClientId()).orElse(null);
        if (client == null)
            throw new CustomException(ROLE_VALIDATE);
        Boolean existed = fucntionRepository.existsByNameAndClientId(form.getName(), form.getClientId());
        if (existed)
            throw new CustomException(FUNCTION_EXISTS);
        FunctionEntity newFunction = roleMapper.toFunctionEntity(form);
        newFunction.setClient(client);
        return fucntionRepository.save(newFunction);
    }
    public boolean validateAuthoritiesForm(AuthoritiesForm form) {
        return form == null || !StringUtils.hasText(form.getClientId())
                || !StringUtils.hasText(form.getName()) || !StringUtils.hasText(form.getDescription());
    }
}
