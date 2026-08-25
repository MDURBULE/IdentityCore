package com.IdentityCore.model.response;

import java.time.Instant;

public record UserResponse(
    String publicId,
    String email,
    String status,
    boolean emailVerified,
    Instant createdAt,
    Instant lastLoginAt
) {}
