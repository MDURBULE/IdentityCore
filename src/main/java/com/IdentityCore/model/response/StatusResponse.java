package com.IdentityCore.model.response;

public record StatusResponse(
    String status,
    String message
) {
    public static StatusResponse success(String message) {
        return new StatusResponse("SUCCESS", message);
    }

    public static StatusResponse failed(String message) {
        return new StatusResponse("FAILED", message);
    }
}
