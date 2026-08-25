package com.IdentityCore.service.impl;

import org.springframework.stereotype.Service;

import com.IdentityCore.model.response.AuthResponse;
import com.IdentityCore.model.response.RegisterResponse;
import com.IdentityCore.service.Interface.AuthService;

@Service
public class AuthServiceImpl implements AuthService{

    @Override
    public RegisterResponse registerUser(String rawEmail, String rawPassword, String ipAddress, String userAgent,
            String baseUrl) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'registerUser'");
    }

    @Override
    public AuthResponse login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress,
            String userAgent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'login'");
    }

    @Override
    public AuthResponse refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'refreshToken'");
    }
    
}
