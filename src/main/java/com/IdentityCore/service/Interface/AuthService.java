package com.IdentityCore.service.Interface;

public interface AuthService {
    public RegisterResult registerUser(String rawEmail, String rawPassword, String ipAddress, String userAgent, String baseUrl);

    public AuthResult login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress, String userAgent);

    public AuthResult refreshToken(String incomingRefreshToken, String clientId, String ipAddress, String userAgent);


    public record RegisterResult(String publicId,String email,String status) {}
    public record AuthResult(String publicId, String accessToken, String refreshToken, String sessionId, boolean mfaRequired) {}
}
