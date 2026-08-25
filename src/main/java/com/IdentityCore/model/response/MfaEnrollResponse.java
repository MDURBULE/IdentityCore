package com.IdentityCore.model.response;

public record MfaEnrollResponse(
    String secret,
    String qrCodeUri,
    String message
) {}
