package com.IdentityCore.model.response;

public record RevokeAllSessionsResponse(
    String status,
    String message,
    int revokedCount
) {}
