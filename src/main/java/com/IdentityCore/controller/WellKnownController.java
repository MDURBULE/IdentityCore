package com.IdentityCore.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.response.HealthResponse;
import com.IdentityCore.model.response.JwksResponse;
import com.IdentityCore.model.response.OpenIdConfigurationResponse;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.utils.Utilities;

import jakarta.servlet.http.HttpServletRequest;

@RestController
public class WellKnownController {

    private final TokenService tokenService;

    public WellKnownController(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @GetMapping("/health")
    public ResponseEntity<HealthResponse> health() {
        return ResponseEntity.ok(new HealthResponse("UP"));
    }

    @GetMapping("/.well-known/jwks.json")
    public ResponseEntity<JwksResponse> jwks() {
        return ResponseEntity.ok(tokenService.getJwks());
    }

    @GetMapping("/.well-known/openid-configuration")
    public ResponseEntity<OpenIdConfigurationResponse> openIdConfiguration(HttpServletRequest req) {
        String baseUrl = Utilities.getBaseUrl(req);
        String issuer = Config.getCpx() != null ? Config.getCpx().getJwtIssuer() : baseUrl;

        OpenIdConfigurationResponse config = new OpenIdConfigurationResponse(
                issuer,
                baseUrl + "/oauth/authorize",
                baseUrl + "/oauth/token",
                baseUrl + "/oauth/userinfo",
                baseUrl + "/oauth/revoke",
                baseUrl + "/.well-known/jwks.json",
                List.of("code", "token", "id_token"),
                List.of("public"),
                List.of("RS256"),
                List.of("openid", "profile", "email", "phone"),
                List.of("S256", "plain")
        );

        return ResponseEntity.ok(config);
    }
}