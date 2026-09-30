package com.IdentityCore.service.impl;

import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.dbhandler.CredentialRepository;
import com.IdentityCore.dbhandler.SecurityEventRepository;
import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.constant.CredentialType;
import com.IdentityCore.model.constant.UserStatus;
import com.IdentityCore.model.entity.Credential;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.objectvalues.Email;
import com.IdentityCore.model.request.RegisterRequest;
import com.IdentityCore.service.Interface.AuthService;
import com.IdentityCore.service.Interface.PasswordService;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TokenService tokenService;
    private final PasswordService passwordService;
    private final CredentialRepository credentialRepository;
    private final SecurityEventRepository securityEventRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           UserService userService,
                           TokenService tokenService,
                           PasswordService passwordService,
                           CredentialRepository credentialRepository,
                           SecurityEventRepository securityEventRepository) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.tokenService = tokenService;
        this.passwordService = passwordService;
        this.credentialRepository = credentialRepository;
        this.securityEventRepository = securityEventRepository;
    }

    @Override
    public RegisterResult registerUser(RegisterRequest request, String ipAddress, String userAgent,
            String baseUrl) {
        Email email = Email.of(request.getEmail());
        String hashedPassword = passwordService.hashPassword(request.getPassword());

        Map<String, Object> procResult = userRepository.registerUser(
                email.getRawEmail(),
                email.getNormalizedEmail(),
                hashedPassword,
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                ipAddress,
                userAgent);

        if (Boolean.FALSE.equals(procResult.get("success"))) {
            String error = (String) procResult.get("error");
            if ("EMAIL_ALREADY_EXISTS".equals(error)) {
                throw new IllegalArgumentException("EMAIL_ALREADY_EXISTS");
            }
            throw new RuntimeException("Registration failed: " + error);
        }

        Long userId = (Long) procResult.get("userId");
        UUID publicId = (UUID) procResult.get("publicId");
        String statusStr = (String) procResult.get("status");

        User user = new User(userId, publicId, email.getRawEmail(), email.getNormalizedEmail(),
                request.getFirstName(), request.getLastName(), request.getPhoneNumber(), null,
                UserStatus.valueOf(statusStr), false, null, null, null);

        userService.sendEmailVerification(user, baseUrl);

        securityEventRepository.recordSecurityEvent(userId, "USER_REGISTERED", ipAddress, userAgent, null,
                "{\"email\":\"" + email.getRawEmail() + "\"}");

        return new RegisterResult(publicId.toString(), email.getRawEmail(), statusStr);
    }

    @Override
    public LoginResult login(String rawEmail, String rawPassword, String clientId, String deviceName, String ipAddress,
            String userAgent) {
        Email email = Email.of(rawEmail);
        Optional<User> optUser = userRepository.findByNormalizedEmail(email.getNormalizedEmail());
        if (optUser.isEmpty()) {
            securityEventRepository.recordSecurityEvent(null, "LOGIN_FAILED", ipAddress, userAgent, clientId,
                    "{\"reason\":\"USER_NOT_FOUND\",\"email\":\"" + rawEmail + "\"}");
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        User user = optUser.get();
        if (user.getStatus() == UserStatus.LOCKED || user.getStatus() == UserStatus.DISABLED || user.getStatus() == UserStatus.DELETED) {
            securityEventRepository.recordSecurityEvent(user.getId(), "LOGIN_BLOCKED", ipAddress, userAgent, clientId,
                    "{\"status\":\"" + user.getStatus() + "\"}");
            throw new IllegalArgumentException("ACCOUNT_" + user.getStatus());
        }

        Optional<Credential> credOpt = credentialRepository.findByUserIdAndType(user.getId(), CredentialType.PASSWORD);
        if (credOpt.isEmpty() || !passwordService.verifyPassword(rawPassword, credOpt.get().getSecretHash())) {
            securityEventRepository.recordSecurityEvent(user.getId(), "LOGIN_FAILED", ipAddress, userAgent, clientId,
                    "{\"reason\":\"PASSWORD_MISMATCH\"}");
            throw new IllegalArgumentException("INVALID_CREDENTIALS");
        }

        userRepository.updateLastLogin(user.getId());

        TokenService.RefreshTokenIssueResult refreshIssue = tokenService.issueInitialRefreshToken(
                user.getId(),
                deviceName,
                ipAddress,
                userAgent);

        String accessToken = tokenService.generateAccessToken(
                user.getPublicId().toString(),
                clientId,
                refreshIssue.sessionId(),
                Set.of("openid", "profile", "email"));

        securityEventRepository.recordSecurityEvent(user.getId(), "LOGIN_SUCCESS", ipAddress, userAgent, clientId,
                "{\"sessionId\":\"" + refreshIssue.sessionId() + "\"}");

        return new LoginResult(user.getPublicId().toString(), accessToken, refreshIssue.rawRefreshToken(),
                refreshIssue.sessionId(), false);
    }

    @Override
    public RefreshResult refreshToken(String incomingRefreshToken, String clientId, String ipAddress,
            String userAgent) {
        Optional<TokenService.RefreshTokenRotationResult> rotOpt = tokenService.rotateRefreshToken(
                incomingRefreshToken,
                ipAddress,
                userAgent,
                null,
                null);

        if (rotOpt.isEmpty()) {
            throw new IllegalArgumentException("INVALID_REFRESH_TOKEN");
        }

        TokenService.RefreshTokenRotationResult rot = rotOpt.get();
        User user = userRepository.findById(rot.userId())
                .orElseThrow(() -> new IllegalStateException("USER_NOT_FOUND"));

        String newAccessToken = tokenService.generateAccessToken(
                user.getPublicId().toString(),
                clientId,
                rot.sessionId(),
                Set.of("openid", "profile", "email"));

        return new RefreshResult(newAccessToken, rot.newRawRefreshToken(), rot.sessionId());
    }
}
