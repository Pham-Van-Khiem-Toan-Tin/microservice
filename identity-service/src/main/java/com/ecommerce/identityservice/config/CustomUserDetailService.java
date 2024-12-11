package com.ecommerce.identityservice.config;

import static com.ecommerce.identityservice.constants.Constants.*;
import com.ecommerce.identityservice.dto.exception.CustomException;
import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
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

        return CustomUserDetail.builder()
                .email(user.getEmail())
                .password(user.getPassword())
                .loginFailCount(user.getLoginFailCount())
                .unlockTime(user.getUnlockTime())
                .lockTime(user.getLockTime())
                .build();
    }
}
