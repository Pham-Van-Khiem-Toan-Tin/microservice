package com.ecommerce.identityservice.config;

import static com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.RoleEntity;
import com.ecommerce.identityservice.entity.RoleFunctionSubFunctionEntity;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.entity.UserRole;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.repository.UserRoleRepository;
import com.ecommerce.identityservice.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserRoleRepository userRoleRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username);
        if (user == null)
            throw new UsernameNotFoundException("Không thể tìm thấy người dùng");
        if (user.getBlock())
            throw new UsernameNotFoundException(LOGIN_BLOCK.getMessage());
        LocalDateTime unlockTime = user.getUnlockTime();
        LocalDateTime currentTime = LocalDateTime.now();
        if (unlockTime != null && unlockTime.isAfter(currentTime))
            throw new UsernameNotFoundException("Tài khoản đang bị khoá. Vui lòng đăng nhập sau" + DateTimeUtils.convertToTimeBetweenString(currentTime, unlockTime));
        List<UserRole> userRoles = user.getRoles();
        Map<String, Set<String>> userRoleClient = new HashMap<>();
        userRoles.stream().forEach(client -> {
            RoleEntity role = client.getRole();
            List<RoleFunctionSubFunctionEntity> roleFunctionSubFunctionEntities = role.getRoleFunctionSubFunction();
            Set<String> roles = new HashSet<>();
            roleFunctionSubFunctionEntities.stream().forEach(item -> {
                roles.add(item.getFunction().getFunctionId());
                roles.add(item.getSubFunction().getSubfunctionId());
            });
            roles.add(role.getRoleId());
            userRoleClient.put(client.getClient().getClientId(), roles);
        });

        return CustomUserDetail.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .roles(userRoleClient)
                .loginFailCount(user.getLoginFailCount())
                .unlockTime(user.getUnlockTime())
                .lockTime(user.getLockTime())
                .build();
    }
}
