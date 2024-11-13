package com.ecommerce.userservice.service.impl;


import com.ecommerce.userservice.dto.UserDTO;
import com.ecommerce.userservice.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;


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
