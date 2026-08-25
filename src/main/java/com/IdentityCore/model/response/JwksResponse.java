package com.IdentityCore.model.response;

import java.util.List;

public record JwksResponse(
    List<JwkKeyDto> keys
) {
    public record JwkKeyDto(
        String kty,
        String use,
        String alg,
        String kid,
        String n,
        String e
    ) {}
}
