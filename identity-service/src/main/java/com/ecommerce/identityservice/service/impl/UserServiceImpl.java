package com.ecommerce.identityservice.service.impl;

import static com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.CustomException;
import com.ecommerce.identityservice.dto.ProfileDTO;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Override
    public ProfileDTO getProfile(String userId) throws CustomException {
        Map<String, Object> userQuery = userRepository.findUserDetailById(userId);
        if (userQuery == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDTO profileDTO = ProfileDTO.toProfile(userQuery);
        return profileDTO;
    }

    @Override
    public ProfileDTO getBaseProfile(String userId) throws CustomException {
        Map<String, Object> userQuery = userRepository.findUserDetailById(userId);
        if (userQuery == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDTO profileDTO = ProfileDTO.toProfileBase(userQuery);
        return profileDTO;
    }
}
