package com.payment.config;

import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final MyPrincipalJwtConvertor myPrincipalJwtConvertor;
    private final JwtDecoder jwtDecoder;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors().disable()
                .csrf().disable()
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers("/v1/api/authenticate/**").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2ResourceServer().jwt(customizer -> customizer
                        .jwtAuthenticationConverter(myPrincipalJwtConvertor)
                        .decoder(jwtDecoder)
                );
        return http.build();
    }
}