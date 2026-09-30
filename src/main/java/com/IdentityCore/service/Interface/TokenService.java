package com.IdentityCore.service.Interface;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.AuthenticatedPrincipal;

import com.IdentityCore.model.request.RefreshToken;
import com.IdentityCore.model.response.JwksResponse;

public interface TokenService {

    AuthenticatedPrincipal verifyAccessToken(String token);

    String generateAccessToken(String publicUserId, String clientId, String sessionId, Set<String> scopes);
    
    String generateRawRefreshToken();

    String hashRefreshToken(String rawToken);

    RefreshTokenIssueResult issueInitialRefreshToken(Long userId, String deviceName, String ipAddress, String userAgent);

    Optional<RefreshTokenRotationResult> rotateRefreshToken(String incomingRawToken, String ipAddress, String userAgent, String userEmail, String userName);

    List<RefreshToken> getActiveSessions(Long userId);

    void revokeSession(Long sessionId, Long userId);

    int revokeAllUserSessions(Long userId, String ipAddress, String userAgent);

    JwksResponse getJwks();

    public record RefreshTokenIssueResult(String rawRefreshToken, String sessionId) {}
    public record RefreshTokenRotationResult(Long userId, String sessionId, String newRawRefreshToken) {}
}
