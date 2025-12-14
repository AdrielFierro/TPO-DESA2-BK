package com.uade.comedor.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Token de autenticación personalizado que incluye el walletId
 */
public class UserAuthenticationToken extends UsernamePasswordAuthenticationToken {
    
    private final String walletId;

    public UserAuthenticationToken(
            Object principal,
            Object credentials,
            Collection<? extends GrantedAuthority> authorities,
            String walletId) {
        super(principal, credentials, authorities);
        this.walletId = walletId;
    }

    public String getWalletId() {
        return walletId;
    }
}
