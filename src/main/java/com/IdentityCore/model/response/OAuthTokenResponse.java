package com.IdentityCore.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthTokenResponse(
    @JsonProperty("access_token") String accessToken,
    @JsonProperty("token_type") String tokenType,
    @JsonProperty("expires_in") long expiresIn,
    String scope
) {}
