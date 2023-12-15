package com.payment.config;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

@Getter
public class MyAuthenticationToken extends AbstractAuthenticationToken {

    private final Long merchantId;

    public MyAuthenticationToken(Long merchantId, Collection<? extends GrantedAuthority> authorities) {
        super(authorities);
        this.merchantId = merchantId;
        setAuthenticated(true);
    }

    @Override
    public Object getCredentials() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return merchantId;
    }

}

