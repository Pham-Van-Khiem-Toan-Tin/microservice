package com.ecommerce.identityservice.service.impl;


import com.ecommerce.identityservice.dto.UserDTO;
import com.ecommerce.identityservice.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl implements UserService {





    @Override
    public UserDTO getProfile(String token, String userId) {

        UserDTO userDTO = new UserDTO();

        return userDTO;
    }
    private String capitalizeFirstLetter(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        String lowerCase = input.toLowerCase();
        return lowerCase.substring(0,1).toUpperCase() + lowerCase.substring(1);
    }
}
