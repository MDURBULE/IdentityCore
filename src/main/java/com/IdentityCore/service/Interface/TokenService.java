package com.IdentityCore.service.Interface;

import org.springframework.security.core.AuthenticatedPrincipal;

public interface TokenService {

    AuthenticatedPrincipal verifyAccessToken(String token);
    
}
