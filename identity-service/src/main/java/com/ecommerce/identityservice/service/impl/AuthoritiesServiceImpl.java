package com.ecommerce.identityservice.service.impl;

import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.ClientEntity;
import com.ecommerce.identityservice.entity.FunctionEntity;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.SubFunctionEntity;
import com.ecommerce.identityservice.form.AuthoritiesForm;
import com.ecommerce.identityservice.form.AuthoritiesWithParentForm;
import com.ecommerce.identityservice.mapper.RoleMapper;
import com.ecommerce.identityservice.repository.ClientRepository;
import com.ecommerce.identityservice.repository.FucntionRepository;
import com.ecommerce.identityservice.repository.RoleRepository;
import com.ecommerce.identityservice.repository.SubFunctionRepository;
import com.ecommerce.identityservice.service.AuthoritiesService;
import com.ecommerce.identityservice.utils.ValidateUtils;
import jakarta.persistence.EntityManager;
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
    SubFunctionRepository subFunctionRepository;
    @Autowired
    ClientRepository clientRepository;
    @Autowired
    EntityManager entityManager;
    @Autowired
    RoleMapper roleMapper;
    @Override
    public RoleEntity createRole(AuthoritiesForm form) throws CustomException {
        ClientEntity client = clientRepository.findById(form.getClientId()).orElse(null);
        if (client == null)
            throw new CustomException(ROLE_VALIDATE);
        Boolean existed = roleRepository.existsByNameAndClientId(form.getId(), form.getClientId());
        if (existed)
            throw new CustomException(ROLE_EXISTS);
        RoleEntity newRole = roleMapper.toRoleEntity(form);
        newRole.setClient(client);
        return roleRepository.save(newRole);
    }

    @Override
    public FunctionEntity createFunction(AuthoritiesForm form) throws CustomException {
        ClientEntity client = clientRepository.findById(form.getClientId()).orElse(null);
        if (client == null)
            throw new CustomException(ROLE_VALIDATE);
        Boolean existed = fucntionRepository.existsByNameAndClientId(form.getId(), form.getClientId());
        if (existed)
            throw new CustomException(FUNCTION_EXISTS);
        FunctionEntity newFunction = roleMapper.toFunctionEntity(form);
        newFunction.setClient(client);
        return fucntionRepository.save(newFunction);
    }

    @Override
    public SubFunctionEntity createSubFunction(AuthoritiesWithParentForm form) throws CustomException {
        ClientEntity client = clientRepository.findById(form.getClientId()).orElse(null);
        if (client == null)
            throw new CustomException(ROLE_VALIDATE);
        Boolean existed = subFunctionRepository.existsByNameAndClientId(form.getId(), form.getClientId());
        if (existed)
            throw new CustomException(FUNCTION_EXISTS);
        SubFunctionEntity newSubFunction = roleMapper.toSubFunctionEntity(form);
        newSubFunction.setClient(client);
        FunctionEntity functionEntity = entityManager.getReference(FunctionEntity.class, form.getParentId());
        newSubFunction.setFunction(functionEntity);
        return subFunctionRepository.save(newSubFunction);
    }

    public boolean validateAuthoritiesForm(AuthoritiesForm form) {
        return form == null || !StringUtils.hasText(form.getClientId())
                || !StringUtils.hasText(form.getName()) || !StringUtils.hasText(form.getDescription());
    }
}
