package com.IdentityCore.service.impl;

import java.time.Instant;
import java.util.UUID;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;
import com.IdentityCore.dbhandler.CredentialRepository;
import com.IdentityCore.dbhandler.SecurityEventRepository;
import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.service.Interface.PasswordService;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.utils.Notification;

@Service
public class PasswordServiceImpl implements PasswordService {

    private final PasswordEncoder passwordEncoder;
    private final CredentialRepository credentialRepository;
    private final UserRepository userRepository;
    private final SecurityEventRepository securityEventRepository;
    private final TokenService tokenService;
    private final Notification notification;

    public PasswordServiceImpl(CredentialRepository credentialRepository,
                               UserRepository userRepository,
                               SecurityEventRepository securityEventRepository,
                               TokenService tokenService,
                               Notification notification) {
        this.passwordEncoder = new BCryptPasswordEncoder(12);
        this.credentialRepository = credentialRepository;
        this.userRepository = userRepository;
        this.securityEventRepository = securityEventRepository;
        this.tokenService = tokenService;
        this.notification = notification;
    }

    @Override
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }

    @Override
    public boolean verifyPassword(String rawPassword, String secretHash) {
        if (secretHash == null) {
            return false;
        }
        return passwordEncoder.matches(rawPassword, secretHash);
    }

    @Override
    public Object initiatePasswordReset(User user, String baseUrl) {
        if (user == null || user.getId() == null) {
            return false;
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String redisKey = "pwd_reset:" + token;
        Config.setRedisKeyWithTtl(redisKey, user.getId().toString(), 15 * 60);

        String resetUrl = baseUrl + "/v1/auth/password/reset?token=" + token;
        String displayName = user.getFirstName() != null ? user.getFirstName() : user.getEmail();
        notification.sendPasswordReset(user.getEmail(), displayName, resetUrl, 15);

        securityEventRepository.recordSecurityEvent(user.getId(), "PASSWORD_RESET_REQUESTED", null, null, null,
                "{\"email\":\"" + user.getEmail() + "\"}");

        return token;
    }

    @Override
    public boolean completePasswordReset(String token, String newPassword, String remoteAddr, String userAgent) {
        if (token == null || newPassword == null || token.isBlank()) {
            return false;
        }

        String redisKey = "pwd_reset:" + token;
        String userIdStr = Config.getKeyFromRedis(redisKey, true);
        if (userIdStr == null || userIdStr.isBlank()) {
            return false;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            String newHash = hashPassword(newPassword);
            boolean updated = credentialRepository.updatePasswordHash(userId, newHash);
            if (!updated) {
                return false;
            }

            tokenService.revokeAllUserSessions(userId, remoteAddr, userAgent);

            userRepository.findById(userId).ifPresent(user -> {
                String displayName = user.getFirstName() != null ? user.getFirstName() : user.getEmail();
                notification.sendPasswordChanged(user.getEmail(), displayName, Instant.now());
            });

            securityEventRepository.recordSecurityEvent(userId, "PASSWORD_RESET_COMPLETED", remoteAddr, userAgent, null,
                    "{\"status\":\"SUCCESS\"}");
            return true;
        } catch (Exception e) {
            Config.getLgr().error("Error completing password reset for token", e);
            return false;
        }
    }
}
