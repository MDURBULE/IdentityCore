package com.IdentityCore.service.impl;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.AuthenticatedPrincipal;
import org.springframework.stereotype.Service;

import com.IdentityCore.model.request.RefreshToken;
import com.IdentityCore.service.Interface.TokenService;

@Service
public class TokenServiceImpl implements TokenService{

    @Override
    public AuthenticatedPrincipal verifyAccessToken(String token) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'verifyAccessToken'");
    }

    @Override
    public String generateAccessToken(String publicUserId, String clientId, String sessionId, Set<String> scopes) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateAccessToken'");
    }
    @Override
    public String generateRawRefreshToken() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'generateRawRefreshToken'");
    }
    @Override
    public String hashRefreshToken(String rawToken) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'hashRefreshToken'");
    }
    @Override
    public RefreshTokenIssueResult issueInitialRefreshToken(Long userId, String deviceName, String ipAddress,
            String userAgent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'issueInitialRefreshToken'");
    }
    @Override
    public Optional<RefreshTokenRotationResult> rotateRefreshToken(String incomingRawToken, String ipAddress,
            String userAgent, String userEmail, String userName) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'rotateRefreshToken'");
    }
    @Override
    public List<RefreshToken> getActiveSessions(Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getActiveSessions'");
    }
    @Override
    public void revokeSession(Long sessionId, Long userId) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revokeSession'");
    }
    @Override
    public int revokeAllUserSessions(Long userId, String ipAddress, String userAgent) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'revokeAllUserSessions'");
    }
    
}
