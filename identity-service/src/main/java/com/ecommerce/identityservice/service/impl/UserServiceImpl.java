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
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.core.SupplierUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

@Service
@Slf4j
@Component
public class UserServiceImpl implements UserService {
    @Autowired
    private UserRepository userRepository;
    @Autowired
    RestTemplate restTemplate;
    @Autowired
    private UserMapper userMapper;
    @Autowired
    private CircuitBreakerFactory circuitBreakerFactory;
    private static final String PAYMENT_SERVICE = "paymentService";
    @Override
    public ProfileDetailDTO getProfile() throws CustomException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(userId);
        if (userEntity == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDetailDTO profileDetailDTO = userMapper.toProfileDetailDTO(userEntity);
        BillingDTO billingDTO = getBillingProfile();
        profileDetailDTO.setBilling(billingDTO);
        return profileDetailDTO;
    }

    public BillingDTO getBillingProfile() {
        String token = getAccessToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(PAYMENT_SERVICE);
        return circuitBreaker.run(() -> restTemplate.exchange("http://localhost:8084/payment/profile",HttpMethod.GET, entity, BillingDTO.class).getBody(),
                throwable -> fallback(throwable));
    }
    public BillingDTO fallback(Throwable ex) {
        log.info("Chạy vào fallback, nguyên nhân: {}", ex.getMessage());
        return new BillingDTO();
    }
    @Override
    public AuthProfileDTO getAuthProfile() throws CustomException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, Object> userQuery = userRepository.findAuthProfile(userId);
        if (userQuery == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        AuthProfileDTO authProfileDTO = UserQueryMapper.toAuthProfileDTO(userQuery);
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
    private String getAccessToken() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Jwt) {
            Jwt jwt = (Jwt) principal;
            return jwt.getTokenValue();
        }
        return null;
    }


}
