package com.IdentityCore.service.impl;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.IdentityCore.service.Interface.TokenService;

@Service
public class TokenServiceImpl implements TokenService{

    @Override
    public AuthenticatedPrincipal verifyAccessToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyAccessToken'");
    }
    
}
