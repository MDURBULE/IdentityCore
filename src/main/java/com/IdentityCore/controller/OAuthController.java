package com.IdentityCore.controller;

import java.util.Set;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.IdentityCore.config.Config;
import com.IdentityCore.model.entity.User;
import com.IdentityCore.model.request.AuthenticatedPrincipal;
import com.IdentityCore.model.response.OAuthAuthorizeResponse;
import com.IdentityCore.model.response.OAuthTokenResponse;
import com.IdentityCore.model.response.OIdcUserInfoResponse;
import com.IdentityCore.model.response.StatusResponse;
import com.IdentityCore.service.Interface.TokenService;
import com.IdentityCore.service.Interface.UserService;

@RestController
@RequestMapping("/oauth")
public class OAuthController {

    private final TokenService tokenService;
    private final UserService userService;

    public OAuthController(TokenService tokenService, UserService userService) {
        this.tokenService = tokenService;
        this.userService = userService;
    }

    @GetMapping("/authorize")
    public ResponseEntity<OAuthAuthorizeResponse> authorize(
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "redirect_uri", required = false) String redirectUri,
            @RequestParam(name = "response_type", defaultValue = "code") String responseType,
            @RequestParam(name = "scope", defaultValue = "openid profile email") String scope,
            @RequestParam(name = "state", required = false) String state,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {

        String authCode = UUID.randomUUID().toString().replace("-", "");
        if (principal != null) {
            Config.setRedisKeyWithTtl("oauth_code:" + authCode, principal.getUserId(), 10 * 60);
        }

        String redirect = (redirectUri != null ? redirectUri : "") +
                (redirectUri != null && redirectUri.contains("?") ? "&" : "?") +
                "code=" + authCode +
                (state != null ? "&state=" + state : "");

        return ResponseEntity.ok(new OAuthAuthorizeResponse(authCode, redirect));
    }

    @PostMapping("/token")
    public ResponseEntity<OAuthTokenResponse> token(
            @RequestParam(name = "grant_type", defaultValue = "authorization_code") String grantType,
            @RequestParam(name = "code", required = false) String code,
            @RequestParam(name = "client_id", required = false) String clientId,
            @RequestParam(name = "refresh_token", required = false) String refreshToken) {

        if ("authorization_code".equals(grantType) && code != null) {
            String userId = Config.getKeyFromRedis("oauth_code:" + code, true);
            if (userId == null) {
                return ResponseEntity.badRequest().build();
            }
            String accessToken = tokenService.generateAccessToken(userId, clientId, null, Set.of("openid", "profile", "email"));
            return ResponseEntity.ok(new OAuthTokenResponse(accessToken, "Bearer", 900, "openid profile email"));
        } else if ("client_credentials".equals(grantType)) {
            String token = tokenService.generateAccessToken("service-account", clientId, null, Set.of("api"));
            return ResponseEntity.ok(new OAuthTokenResponse(token, "Bearer", 900, "api"));
        }

        return ResponseEntity.badRequest().build();
    }

    @GetMapping("/userinfo")
    public ResponseEntity<OIdcUserInfoResponse> userInfo(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        if (principal == null || principal.getUserId() == null) {
            return ResponseEntity.status(401).build();
        }

        User user = userService.findByPublicId(UUID.fromString(principal.getUserId()))
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        String fullName = null;
        if (user.getFirstName() != null && user.getLastName() != null) {
            fullName = user.getFirstName() + " " + user.getLastName();
        } else if (user.getFirstName() != null) {
            fullName = user.getFirstName();
        }

        OIdcUserInfoResponse response = new OIdcUserInfoResponse(
                user.getPublicId().toString(),
                user.getEmail(),
                user.isEmailVerified(),
                fullName,
                user.getFirstName(),
                user.getLastName(),
                user.getPhoneNumber(),
                user.getAvatarUrl(),
                user.getStatus() != null ? user.getStatus().name() : null
        );

        return ResponseEntity.ok(response);
    }

    @PostMapping("/revoke")
    public ResponseEntity<StatusResponse> revoke(
            @RequestParam("token") String token,
            @AuthenticationPrincipal AuthenticatedPrincipal principal) {
        return ResponseEntity.ok(StatusResponse.success("Token revoked successfully"));
    }
}
