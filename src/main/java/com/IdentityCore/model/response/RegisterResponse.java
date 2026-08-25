package com.IdentityCore.model.response;

public record RegisterResponse(
    String publicId,
    String email,
    String status,
    String message
) {}
