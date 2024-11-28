package com.ecommerce.identityservice.service.impl;

import static com.ecommerce.identityservice.constants.Constants.*;

import com.ecommerce.identityservice.dto.BillingDTO;
import com.ecommerce.identityservice.dto.ProfileDetailDTO;
import com.ecommerce.identityservice.dto.TestDTO;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.dto.AuthProfileDTO;
import com.ecommerce.identityservice.entity.RoleFunctionSubFunctionEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.mapper.UserMapper;
import com.ecommerce.identityservice.mapper.UserQueryMapper;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    RestTemplate restTemplate;
    @Override
    public ProfileDetailDTO getProfile() throws CustomException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(userId);
        if (userEntity == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDetailDTO profileDetailDTO = UserMapper.INSTANCE.toProfileDetailDTO(userEntity);
        profileDetailDTO.setRole(userEntity.getRole().getName());
//        BillingDTO billing = getBillingProfile();
//        profileDetailDTO.setBilling(billing);
        return profileDetailDTO;
    }

    @Override
    public AuthProfileDTO getAuthProfile() throws CustomException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> userQuery = userRepository.findAuthProfile(userId);
        if (userQuery == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        AuthProfileDTO authProfileDTO = UserQueryMapper.toProfileBaseDTO(userQuery);
        return authProfileDTO;
    }

    @Override
    public TestDTO test() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(userId);
        TestDTO testDTO = new TestDTO();
        List<RoleFunctionSubFunctionEntity> functionSubFunctionEntityList = user.getRole().getRoleFunctionSubFunction();
        Set<String> functions = functionSubFunctionEntityList.stream().map(item -> item.getFunction().getId()).collect(Collectors.toSet());
        Set<String> subfunctions = functionSubFunctionEntityList.stream().map(item -> item.getSubFunction().getId()).collect(Collectors.toSet());
        testDTO.setEmail(user.getEmail());
        testDTO.setRole(user.getRole().getName());
        testDTO.setFunctions(functions.stream().toList());
        testDTO.setSubfunctions(subfunctions.stream().toList());
        return testDTO;
    }

    @CircuitBreaker(name = "paymentService", fallbackMethod = "billingProfileFallback")
    private BillingDTO getBillingProfile() {
        String token = getAccessToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        BillingDTO billingDTO = restTemplate.exchange("http://localhost:8084/payment/profile", HttpMethod.GET, entity, BillingDTO.class).getBody();
        return billingDTO;
    }
    public void billingProfileFallback(Throwable throwable) throws CustomException {
        throw new CustomException(INTERNAL_SERVER);
    }
    private String getAccessToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return jwt.getTokenValue();
        }
        return null;
    }
}
