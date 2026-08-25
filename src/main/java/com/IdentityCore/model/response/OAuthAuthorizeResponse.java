package com.IdentityCore.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public record OAuthAuthorizeResponse(
    String code,
    @JsonProperty("redirect_url") String redirectUrl
) {}
