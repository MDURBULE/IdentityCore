package com.IdentityCore.service.Interface;

import com.IdentityCore.model.request.RegisterRequest;

public interface AuthService {
    public RegisterResult registerUser(RegisterRequest request, String ipAddress, String userAgent, String baseUrl);

    public LoginResult login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress, String userAgent);

    public RefreshResult refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent);


    public record RegisterResult(String publicId, String email, String status) {}
    public record LoginResult(String publicId, String accessToken, String refreshToken, String sessionId, boolean mfaRequired) {}
    public record RefreshResult(String accessToken, String refreshToken, String sessionId) {}
    
}
