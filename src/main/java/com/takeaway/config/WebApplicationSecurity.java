package com.takeaway.config;

import com.takeaway.service.CustomUserDetailService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@AllArgsConstructor
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class WebApplicationSecurity {
    private final CustomUserDetailService userDetailsService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http.csrf().disable()
                .authorizeRequests()
                .antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources", "/configuration/security",
                        "/swagger-ui/**", "/webjars/**", "/swagger-resources/configuration/ui",
                        "/swagger-resources/configuration/security").permitAll()
//                .antMatchers(HttpMethod.POST, "/employees").hasAnyRole("CREATED")
//                .antMatchers(HttpMethod.PUT, "/employees/**").hasAnyRole("UPDATED")
//                .antMatchers(HttpMethod.DELETE, "/employees/**").hasAnyRole("DELETED")
                .antMatchers(HttpMethod.GET, "/employees/**").permitAll()
                .anyRequest().permitAll()
                .and()
                .authenticationProvider(authenticationProvider())
                .httpBasic()
                .and()
                .build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        var provider = new DaoAuthenticationProvider();
        provider.setPasswordEncoder(passwordEncoder());
        provider.setUserDetailsService(userDetailsService);
        return provider;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
