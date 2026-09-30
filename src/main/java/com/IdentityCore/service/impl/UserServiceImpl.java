package com.IdentityCore.service.impl;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.IdentityCore.config.Config;
import com.IdentityCore.dbhandler.SecurityEventRepository;
import com.IdentityCore.dbhandler.UserRepository;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.objectvalues.Email;
import com.IdentityCore.model.request.UpdateProfileRequest;
import com.IdentityCore.service.Interface.UserService;
import com.IdentityCore.utils.Notification;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final SecurityEventRepository securityEventRepository;
    private final Notification notification;

    public UserServiceImpl(UserRepository userRepository,
                           SecurityEventRepository securityEventRepository,
                           Notification notification) {
        this.userRepository = userRepository;
        this.securityEventRepository = securityEventRepository;
        this.notification = notification;
    }

    @Override
    public void sendEmailVerification(User user, String baseUrl) {
        if (user == null || user.getId() == null) {
            return;
        }

        String token = UUID.randomUUID().toString().replace("-", "") + UUID.randomUUID().toString().replace("-", "");
        String redisKey = "email_verify:" + token;
        Config.setRedisKeyWithTtl(redisKey, user.getId().toString(), 24 * 3600);

        String verificationUrl = baseUrl + "/v1/auth/email/verify?token=" + token;
        String displayName = user.getFirstName() != null ? user.getFirstName() : user.getEmail();
        notification.sendEmailVerification(user.getEmail(), displayName, verificationUrl, 24 * 60);

        securityEventRepository.recordSecurityEvent(user.getId(), "EMAIL_VERIFICATION_SENT", null, null, null,
                "{\"email\":\"" + user.getEmail() + "\"}");
    }

    @Override
    public Optional<User> findByPublicId(UUID publicId) {
        if (publicId == null) {
            return Optional.empty();
        }
        return userRepository.findByPublicId(publicId);
    }

    @Override
    public boolean verifyEmailToken(String token, String remoteAddr, String userAgent) {
        if (token == null || token.isBlank()) {
            return false;
        }

        String redisKey = "email_verify:" + token;
        String userIdStr = Config.getKeyFromRedis(redisKey, true);
        if (userIdStr == null || userIdStr.isBlank()) {
            return false;
        }

        try {
            Long userId = Long.parseLong(userIdStr);
            boolean verified = userRepository.verifyEmail(userId);
            if (verified) {
                securityEventRepository.recordSecurityEvent(userId, "EMAIL_VERIFIED", remoteAddr, userAgent, null,
                        "{\"status\":\"SUCCESS\"}");
                return true;
            }
        } catch (Exception e) {
            Config.getLgr().error("Failed to verify email for token", e);
        }
        return false;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        if (email == null || email.isBlank()) {
            return Optional.empty();
        }
        try {
            String normalized = Email.of(email).getNormalizedEmail();
            return userRepository.findByNormalizedEmail(normalized);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    @Override
    public User updateProfile(UUID publicId, UpdateProfileRequest request) {
        User user = findByPublicId(publicId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userRepository.updateProfile(user.getId(),
                request.getFirstName(),
                request.getLastName(),
                request.getPhoneNumber(),
                request.getAvatarUrl());

        return findByPublicId(publicId)
                .orElseThrow(() -> new IllegalStateException("User not found after update"));
    }
}
