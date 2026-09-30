package com.IdentityCore.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OIdcUserInfoResponse(
    String sub,
    String email,
    @JsonProperty("email_verified") boolean emailVerified,
    String name,
    @JsonProperty("given_name") String givenName,
    @JsonProperty("family_name") String familyName,
    @JsonProperty("phone_number") String phoneNumber,
    String picture,
    String status
) {}
