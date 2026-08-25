package com.IdentityCore.model.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record OpenIdConfigurationResponse(
    String issuer,
    @JsonProperty("authorization_endpoint") String authorizationEndpoint,
    @JsonProperty("token_endpoint") String tokenEndpoint,
    @JsonProperty("userinfo_endpoint") String userinfoEndpoint,
    @JsonProperty("revocation_endpoint") String revocationEndpoint,
    @JsonProperty("jwks_uri") String jwksUri,
    @JsonProperty("response_types_supported") List<String> responseTypesSupported,
    @JsonProperty("subject_types_supported") List<String> subjectTypesSupported,
    @JsonProperty("id_token_signing_alg_values_supported") List<String> idTokenSigningAlgValuesSupported,
    @JsonProperty("scopes_supported") List<String> scopesSupported,
    @JsonProperty("code_challenge_methods_supported") List<String> codeChallengeMethodsSupported
) {}
