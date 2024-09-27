package com.ecommerce.identityservice.config;

import com.ecommerce.identityservice.entity.User;
import com.ecommerce.identityservice.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findById(email).orElseThrow(() -> new UsernameNotFoundException("User not found"));
        return new CustomUserDetail(
                user.getEmail(),
                user.getName(),
                user.getPassword(),
                user.getRole()
        );
    }
}
