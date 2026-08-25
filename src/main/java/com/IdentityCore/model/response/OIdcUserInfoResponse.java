package com.IdentityCore.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OIdcUserInfoResponse(
    String sub,
    String email,
    @JsonProperty("email_verified") boolean emailVerified,
    String status
) {}
