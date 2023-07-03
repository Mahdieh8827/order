package com.takeaway.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailService implements UserDetailsService {

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var encoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        return User.withUsername("admin")
                .password(encoder.encode("123"))
                .authorities("DELETED,CREATED,UPDATED").build();
    }
}
