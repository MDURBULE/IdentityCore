package com.IdentityCore.model.response;

import java.time.Instant;

public record SessionResponse(
    Long id,
    String deviceName,
    String ipAddress,
    String userAgent,
    Instant expiresAt,
    Instant createdAt
) {}
