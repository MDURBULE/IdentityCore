package com.IdentityCore.service.Interface;

import com.IdentityCore.model.response.AuthResponse;
import com.IdentityCore.model.response.RegisterResponse;

public interface AuthService {
    public RegisterResponse registerUser(String rawEmail, String rawPassword, String ipAddress, String userAgent, String baseUrl);

    public AuthResponse login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress, String userAgent);

    public AuthResponse refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent);
}
