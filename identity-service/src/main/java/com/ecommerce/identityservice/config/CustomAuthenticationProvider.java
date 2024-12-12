package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.repository.UserRepository;
import com.ecommerce.identityservice.utils.DateTimeUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    @Autowired
    CustomUserDetailService userDetailService;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    UserRepository userRepository;
    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String email = authentication.getName();
        String password = authentication.getCredentials().toString();
        CustomUserDetail userDetails = (CustomUserDetail) userDetailService.loadUserByUsername(email);
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            Integer loginFailCount = userDetails.getLoginFailCount();
            LocalDateTime newUnlockTime;
            LocalDateTime currentTime = LocalDateTime.now();
            Integer newLoginFailCount;
            if (loginFailCount == 5) {
                newUnlockTime = currentTime.plusHours(12);
                newLoginFailCount = 0;
            } else {
                newLoginFailCount = loginFailCount + 1;
                newUnlockTime = currentTime.plusMinutes(newLoginFailCount);
            }
            userRepository.updateTemporaryLock(userDetails.getUsername(), currentTime, newUnlockTime, newLoginFailCount);
            throw new AuthenticationException("Thông tin email hoặc mật khẩu không đúng. Tài khoản của bạn bị khoá đến " + DateTimeUtils.convertToTimeString(newUnlockTime, "HH:mm:ss dd-MM-yyyy")) {};
        }
        Authentication authenticated = new UsernamePasswordAuthenticationToken(userDetails, password, userDetails.getAuthorities());
        return authenticated;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
