package com.ecommerce.identityservice.service.impl;

import static com.ecommerce.identityservice.constants.Constants.*;

import com.ecommerce.identityservice.dto.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.*;
import com.ecommerce.identityservice.form.BillingForm;
import com.ecommerce.identityservice.form.UpdateProfileForm;
import com.ecommerce.identityservice.mapper.UserMapper;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.service.UserService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.circuitbreaker.CircuitBreaker;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import java.util.*;
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
    public ProfileDTO getProfile() throws CustomException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        UserEntity userEntity = userRepository.findByEmail(userId);
        if (userEntity == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDTO profileDTO = userMapper.toProfileDTO(userEntity);
        CustomAuthenticationDetail detail = (CustomAuthenticationDetail) authentication.getDetails();
        RoleEntity role = userEntity.getRoles().stream().filter(item -> item.getRole().getClient().getClientId().equals(detail.getClientId())).collect(Collectors.toList()).get(0).getRole();
        profileDTO.setRole(role.getRoleName());
        BillingDTO billingDTO = getBillingProfile();
        profileDTO.setBilling(billingDTO);
        return profileDTO;
    }

    @Override
    public ProfileDetailDTO getProfileDetail() throws CustomException {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findByEmail(userId);
        if (userEntity == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        ProfileDetailDTO profileDetailDTO = userMapper.toProfileDetailDTO(userEntity);
        BillingDTO billingDTO = getBillingProfile();
        profileDetailDTO.setBilling(billingDTO);
        return profileDetailDTO;
    }

    @Override
    @Transactional(rollbackOn = Exception.class)
    public void updateProfile(UpdateProfileForm form) throws CustomException {
        if (form == null)
            throw new CustomException(PROFILE_VALIDATE);
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity userEntity = userRepository.findById(userId).orElse(null);
        if (userEntity == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        if (StringUtils.hasText(form.getFirstName()))
            userEntity.setFirstName(form.getFirstName().trim());
        if (StringUtils.hasText(form.getLastName()))
            userEntity.setLastName(form.getLastName().trim());
        if (StringUtils.hasText(form.getPhoneNumber()))
            userEntity.setPhoneNumber(form.getPhoneNumber().trim());
        BillingForm billingForm = form.getBilling();
        if (hasBillingData(billingForm)) {
            BillingDTO billingDTO = updateBilling(form.getBilling());
            if (billingDTO == null) {
                throw new CustomException(PROFILE_UPDATE_FAIL);
            }
        }
        userRepository.save(userEntity);
    }
    public boolean hasBillingData(BillingForm form) {
        return StringUtils.hasText(form.getFirstName()) ||
                StringUtils.hasText(form.getLastName()) ||
                StringUtils.hasText(form.getAddress()) ||
                StringUtils.hasText(form.getCountry()) ||
                form.getStates() != null ||
                StringUtils.hasText(form.getZipCode()) ||
                StringUtils.hasText(form.getCompany()) ||
                StringUtils.hasText(form.getPhoneNumber());
    }
    public BillingDTO updateBilling(BillingForm form) {
        String token = getAccessToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BillingForm> entity = new HttpEntity<>(form,httpHeaders);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(PAYMENT_SERVICE);
        return circuitBreaker.run(() -> restTemplate.exchange("http://localhost:8084/payment/profile", HttpMethod.PUT, entity, BillingDTO.class).getBody(),
                throwable -> null);
    }

    public BillingDTO getBillingProfile() {
        String token = getAccessToken();
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add(HttpHeaders.AUTHORIZATION, "Bearer " + token);
        HttpEntity<String> entity = new HttpEntity<>(httpHeaders);
        CircuitBreaker circuitBreaker = circuitBreakerFactory.create(PAYMENT_SERVICE);
        return circuitBreaker.run(() -> restTemplate.exchange("http://localhost:8084/payment/profile", HttpMethod.GET, entity, BillingDTO.class).getBody(),
                throwable -> fallback(throwable));
    }

    public BillingDTO fallback(Throwable ex) {
        ex.printStackTrace();
        log.info("get billing profile fail: {}", ex.getMessage());
        return new BillingDTO(); // Giá trị mặc định

    }

    @Override
    public AuthProfileDTO getAuthProfile() throws CustomException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userId = authentication.getName();
        UserEntity user = userRepository.findByEmail(userId);
        if (user == null)
            throw new CustomException(PROFILE_NOT_FOUND);
        AuthProfileDTO authProfileDTO = UserMapper.toAuthProfileDTO(user);
        CustomAuthenticationDetail detail = (CustomAuthenticationDetail) authentication.getDetails();
        RoleEntity role = user.getRoles().stream().filter(item -> item.getRole().getClient().getClientId().equals(detail.getClientId())).collect(Collectors.toList()).get(0).getRole();
        authProfileDTO.setRole(role.getRoleId());
        List<RoleFunctionSubFunctionEntity> functionEntities = role.getRoleFunctionSubFunction();
        Set<String> functions = new HashSet<>();
        Set<String> subFunctions = new HashSet<>();
        functionEntities.stream().forEach(item -> {
            functions.add(item.getFunction().getFunctionId());
            subFunctions.add(item.getSubFunction().getSubfunctionId());
        });
        authProfileDTO.setFunctions(functions);
        authProfileDTO.setSubfunctions(subFunctions);
        return authProfileDTO;
    }

    @Override
    public TestDTO test() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        UserEntity user = userRepository.findByEmail(userId);
        TestDTO testDTO = new TestDTO();
        List<UserRole> userRoles = user.getRoles().stream().flatMap(item -> item.getRole().getFunctions());
        List<RoleFunctionSubFunctionEntity> functionSubFunctionEntityList = userRoles.stream().
        Set<String> functions = functionSubFunctionEntityList.stream().map(item -> item.getFunction().getId()).collect(Collectors.toSet());
        Set<String> subfunctions = functionSubFunctionEntityList.stream().map(item -> item.getSubFunction().getId()).collect(Collectors.toSet());
        testDTO.setEmail(user.getEmail());
        testDTO.setRole(user.getRole().getRoleName());
        testDTO.setFunctions(functions.stream().toList());
        testDTO.setSubfunctions(subfunctions.stream().toList());

        return testDTO;
    }

    private String getAccessToken() {
        CustomAuthenticationDetail detail = (CustomAuthenticationDetail) SecurityContextHolder.getContext().getAuthentication().getDetails();
        return detail.getToken();
    }


}
