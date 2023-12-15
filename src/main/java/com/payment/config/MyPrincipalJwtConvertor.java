package com.payment.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.List;

@Configuration
public class MyPrincipalJwtConvertor implements Converter<Jwt, MyAuthenticationToken> {

    private final String rolePayment;


    public MyPrincipalJwtConvertor(@Value("${role.payment}") String rolePayment) {
        this.rolePayment = rolePayment;
    }

    @Override
    public MyAuthenticationToken convert(Jwt jwt) {
        Long merchantId = Long.parseLong(jwt.getClaims().get("merchantId").toString());
        return new MyAuthenticationToken(merchantId,List.of(new SimpleGrantedAuthority(rolePayment)));}

    @Override
    public <U> Converter<Jwt, U> andThen(Converter<? super MyAuthenticationToken, ? extends U> after) {
        return null;
    }
}




