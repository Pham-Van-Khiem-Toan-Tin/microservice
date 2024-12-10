package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.entity.UserEntity;
import com.ecommerce.identityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByEmail(username);
        if (user == null)
            throw new UsernameNotFoundException("Không thể tìm thấy người dùng");
        return new User(user.getEmail(), user.getPassword(), Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().getId())));
    }
}
