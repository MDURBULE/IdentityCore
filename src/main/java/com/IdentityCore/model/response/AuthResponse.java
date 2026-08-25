package com.IdentityCore.model.response;

public record AuthResponse(
    String publicId,
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    boolean mfaRequired
) {
    public static AuthResponse ofSuccess(String publicId, String accessToken, String refreshToken, long expiresIn) {
        return new AuthResponse(publicId, accessToken, refreshToken, "Bearer", expiresIn, false);
    }

    public static AuthResponse ofMfaRequired(String publicId) {
        return new AuthResponse(publicId, null, null, null, 0, true);
    }
}
