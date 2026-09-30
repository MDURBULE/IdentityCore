package com.IdentityCore.model.response;

import java.time.Instant;

import com.IdentityCore.model.entity.User;

public record UserResponse(
    String publicId,
    String email,
    String firstName,
    String lastName,
    String phoneNumber,
    String avatarUrl,
    String status,
    boolean emailVerified,
    Instant createdAt,
    Instant lastLoginAt
) {
    public static UserResponse from(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
            user.getPublicId() != null ? user.getPublicId().toString() : null,
            user.getEmail(),
            user.getFirstName(),
            user.getLastName(),
            user.getPhoneNumber(),
            user.getAvatarUrl(),
            user.getStatus() != null ? user.getStatus().name() : null,
            user.isEmailVerified(),
            user.getCreatedAt(),
            user.getLastLoginAt()
        );
    }
}
