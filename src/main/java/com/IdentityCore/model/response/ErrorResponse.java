package com.IdentityCore.model.response;

public record ErrorResponse(
    ErrorEnvelope error
) {
    public record ErrorEnvelope(
        String code,
        String message,
        String requestId
    ) {}

    public static ErrorResponse of(String code, String message, String requestId) {
        return new ErrorResponse(new ErrorEnvelope(code, message, requestId));
    }
}
